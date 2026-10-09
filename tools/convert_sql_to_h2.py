"""把 MySQL 版 contract.sql 转换成 H2（MODE=MySQL）可直接执行的 schema.sql + data.sql。

用途：本机没装 MySQL 时，用 H2 内存库跑通「前端 → Java → Python」的本地开发链路。
生产环境仍然走 MySQL，本脚本产物只服务于 dev/h2 profile。

转换规则（MySQL → H2 2.x）：
  1. 去掉 CREATE DATABASE / USE / DEFAULT CHARACTER SET / DEFAULT COLLATE
  2. 去掉反引号（列名/表名都是普通标识符，已确认没有命中 H2 保留字）
  3. 去掉列定义尾部的 inline COMMENT '...'
  4. 去掉 ON UPDATE CURRENT_TIMESTAMP（H2 不支持）
  5. 去掉建表收尾的 ENGINE=... CHARSET=... COLLATE=... COMMENT='...'
  6. 普通 KEY `name` (cols) → 丢弃（H2 不支持 MySQL 内联索引语法）
     UNIQUE KEY `name` (cols) → CONSTRAINT name UNIQUE (cols)

用法：
    python tools/convert_sql_to_h2.py \
        --src ../../contract.sql \
        --out-dir src/main/resources/db/h2
"""

from __future__ import annotations

import argparse
import re
from pathlib import Path

# 列定义尾部： COMMENT '...'（MySQL 里字符串内的单引号用 '' 转义）
COMMENT_RE = re.compile(r"\s+COMMENT\s+'(?:[^']|'')*'", re.IGNORECASE)
# ON UPDATE CURRENT_TIMESTAMP / ON UPDATE NOW()
ON_UPDATE_RE = re.compile(r"\s+ON\s+UPDATE\s+(?:CURRENT_TIMESTAMP|NOW\(\)|LOCALTIMESTAMP)", re.IGNORECASE)
# 建表收尾的存储引擎/字符集/排序规则/表注释
TABLE_SUFFIX_RE = re.compile(
    r"\)\s*ENGINE\s*=\s*\w+.*?;\s*$", re.IGNORECASE | re.DOTALL
)
# UNIQUE KEY `name` (cols)
UNIQUE_KEY_RE = re.compile(r"^\s*UNIQUE\s+KEY\s+`?(\w+)`?\s*\((.+?)\)\s*(,?)\s*$", re.IGNORECASE)
# 普通 KEY `name` (cols) / INDEX `name` (cols)
PLAIN_KEY_RE = re.compile(r"^\s*(?:KEY|INDEX)\s+`?\w+`?\s*\(.+?\)\s*,?\s*$", re.IGNORECASE)
# CREATE TABLE `name` ( —— 用于跟踪当前表名
TABLE_NAME_RE = re.compile(r"^\s*CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?`?(\w+)`?\s*\(", re.IGNORECASE)


def convert_lines(sql: str) -> tuple[list[str], list[str]]:
    """返回 (ddl_lines, dml_lines)。

    ⚠️ INSERT 语句是多行的（一行 `INSERT INTO ... VALUES` 后面跟着若干 `(...),` 值行），
    必须按「语句」而不是按「行」来判定归属，否则值行会被当成 DDL 塞进 schema.sql。
    """
    ddl: list[str] = []
    dml: list[str] = []
    skip_continuation = False
    in_insert = False
    current_table = ""

    for raw_line in sql.splitlines():
        line = raw_line.rstrip("\n")
        stripped_upper = line.strip().upper()

        # --- INSERT 语句体中：整段（含多行值）都归 data.sql ---
        if in_insert:
            dml.append(line.replace("`", ""))
            if line.rstrip().endswith(";"):
                in_insert = False
            continue

        # --- 跳过 MySQL 独有的库级语句 ---
        if stripped_upper.startswith("CREATE DATABASE") or skip_continuation:
            skip_continuation = not line.rstrip().endswith(";")
            continue
        if stripped_upper.startswith("DEFAULT CHARACTER SET") or stripped_upper.startswith("DEFAULT COLLATE"):
            skip_continuation = not line.rstrip().endswith(";")
            continue
        if stripped_upper.startswith("USE "):
            continue

        # --- INSERT 起始行：交给 data.sql，并进入语句体模式 ---
        if stripped_upper.startswith("INSERT INTO"):
            dml.append(line.replace("`", ""))
            if not line.rstrip().endswith(";"):
                in_insert = True
            continue

        # 记录当前正在建的表名，用于给约束名加前缀（H2 的约束名是全库唯一的，
        # MySQL 里同名约束出现在不同表是允许的，直接搬过去会报 already exists）
        m_table = TABLE_NAME_RE.match(line)
        if m_table:
            current_table = m_table.group(1)

        # --- 建表尾部： ) ENGINE=InnoDB ... ; ---
        if TABLE_SUFFIX_RE.search(line):
            ddl.append(TABLE_SUFFIX_RE.sub(");", line))
            continue

        # --- 普通 KEY：直接丢弃 ---
        if PLAIN_KEY_RE.match(line):
            continue

        # --- UNIQUE KEY：转成表级 UNIQUE 约束 ---
        m = UNIQUE_KEY_RE.match(line)
        if m:
            name, cols, comma = m.group(1), m.group(2), m.group(3)
            name = current_table + "_" + name if current_table else name
            ddl.append(f"  CONSTRAINT {name} UNIQUE ({cols.replace('`', '')}){comma}")
            continue

        # --- 常规行：去 COMMENT / ON UPDATE / 反引号 ---
        line = COMMENT_RE.sub("", line)
        line = ON_UPDATE_RE.sub("", line)
        ddl.append(line.replace("`", ""))

    return _drop_dangling_commas(ddl), dml


def _drop_dangling_commas(lines: list[str]) -> list[str]:
    """丢弃 KEY 行后，建表块里最后一行会留下多余的逗号，例如：
        PRIMARY KEY (id),
        );
    这里把紧跟在 ')'、') ;' 之前的尾随逗号去掉。
    """
    out: list[str] = []
    for line in lines:
        if out and re.match(r"^\s*\)\s*;?\s*$", line) and out[-1].rstrip().endswith(","):
            out[-1] = out[-1].rstrip()[:-1]
        out.append(line)
    return out


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--src", required=True, help="MySQL 版 contract.sql 路径")
    parser.add_argument("--out-dir", required=True, help="输出目录")
    args = parser.parse_args()

    src = Path(args.src).resolve()
    out_dir = Path(args.out_dir).resolve()
    out_dir.mkdir(parents=True, exist_ok=True)

    sql = src.read_text(encoding="utf-8")
    ddl, dml = convert_lines(sql)

    schema_path = out_dir / "schema.sql"
    data_path = out_dir / "data.sql"

    header = f"-- 由 tools/convert_sql_to_h2.py 从 {src.name} 自动生成，请勿手工修改。\n\n"
    # newline="\n"：强制 LF，避免 Windows 下写出 CRLF 影响跨平台一致性
    schema_path.write_text(header + "\n".join(ddl).strip() + "\n", encoding="utf-8", newline="\n")
    data_path.write_text(header + "\n".join(dml).strip() + "\n", encoding="utf-8", newline="\n")

    # 自检：schema.sql 里不应该出现游离的 "(" 值行（这是 INSERT 多行值被误判为 DDL 的典型症状）
    orphan = [ln for ln in ddl if re.match(r"^\s*\(", ln)]
    if orphan:
        print(f"⚠️  schema.sql 中疑似有 {len(orphan)} 行游离值行，请检查 INSERT 语句切分逻辑")

    print(f"schema.sql -> {schema_path}  ({len(ddl)} 行)")
    print(f"data.sql   -> {data_path}  ({len(dml)} 行)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
