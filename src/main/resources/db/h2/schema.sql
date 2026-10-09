-- 由 tools/convert_sql_to_h2.py 从 contract.sql 自动生成，请勿手工修改。

-- ============================================================
-- 审计字段统一规范（适用于所有「单表」，中间表与 append-only 日志表除外）：
--   created_time  DATETIME DEFAULT CURRENT_TIMESTAMP              创建时间
--   updated_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE    更新时间
--   deleted       TINYINT NOT NULL DEFAULT 0  (0-正常，-1-删除)    逻辑删除
--   version       INT NOT NULL DEFAULT 0                          乐观锁版本号
-- 跳过的表：sys_role_menu（纯中间表）
--           sys_operation_log、integration_oa_log、
--           integration_sign_record、integration_api_log（append-only 日志表）
-- ============================================================

DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
  id BIGINT NOT NULL AUTO_INCREMENT,
  parent_id BIGINT NOT NULL DEFAULT 0,
  menu_name VARCHAR(100) NOT NULL,
  menu_type VARCHAR(20) NOT NULL,
  path VARCHAR(255) DEFAULT '',
  component VARCHAR(255) DEFAULT '',
  icon VARCHAR(100) DEFAULT '',
  permission VARCHAR(200) DEFAULT '',
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_by VARCHAR(50) DEFAULT '',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) DEFAULT '',
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  role_name VARCHAR(100) NOT NULL,
  role_code VARCHAR(100) NOT NULL,
  description VARCHAR(500) DEFAULT '',
  status TINYINT NOT NULL DEFAULT 1,
  created_by VARCHAR(50) DEFAULT '',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) DEFAULT '',
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT sys_role_uk_role_code UNIQUE (role_code)
);

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(100) NOT NULL,
  role_id BIGINT NOT NULL,
  role_name VARCHAR(100) DEFAULT '',
  department VARCHAR(100) DEFAULT '',
  phone VARCHAR(20) DEFAULT '',
  email VARCHAR(100) DEFAULT '',
  avatar VARCHAR(255) DEFAULT '',
  status TINYINT NOT NULL DEFAULT 1,
  last_login_time DATETIME DEFAULT NULL,
  last_login_ip VARCHAR(50) DEFAULT '',
  created_by VARCHAR(50) DEFAULT '',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) DEFAULT '',
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT sys_user_uk_username UNIQUE (username)
);

-- 中间表（纯关联表）：sys_role_menu —— 不加审计字段
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
  id BIGINT NOT NULL AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT sys_role_menu_uk_role_menu UNIQUE (role_id, menu_id)
);

DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
  id BIGINT NOT NULL AUTO_INCREMENT,
  config_key VARCHAR(100) NOT NULL,
  config_value TEXT DEFAULT NULL,
  config_name VARCHAR(100) DEFAULT '',
  config_type VARCHAR(50) DEFAULT '',
  description VARCHAR(500) DEFAULT '',
  status TINYINT NOT NULL DEFAULT 1,
  created_by VARCHAR(50) DEFAULT '',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) DEFAULT '',
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT sys_config_uk_config_key UNIQUE (config_key)
);

DROP TABLE IF EXISTS sys_backup;
CREATE TABLE sys_backup (
  id BIGINT NOT NULL AUTO_INCREMENT,
  backup_name VARCHAR(200) NOT NULL,
  backup_type VARCHAR(50) NOT NULL,
  backup_path VARCHAR(500) NOT NULL,
  backup_size BIGINT DEFAULT 0,
  backup_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  backup_time DATETIME DEFAULT NULL,
  file_count INT DEFAULT 0,
  description VARCHAR(500) DEFAULT '',
  created_by VARCHAR(50) DEFAULT '',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- append-only 日志表：sys_operation_log —— 不加 deleted/version，保留原 created_time
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  user_name VARCHAR(100) NOT NULL,
  operation_type VARCHAR(50) NOT NULL,
  module VARCHAR(100) NOT NULL,
  description VARCHAR(500) NOT NULL,
  request_url VARCHAR(500) DEFAULT '',
  request_method VARCHAR(10) DEFAULT '',
  request_param TEXT DEFAULT NULL,
  response_result TEXT DEFAULT NULL,
  ip_address VARCHAR(50) DEFAULT '',
  user_agent VARCHAR(500) DEFAULT '',
  execution_time INT DEFAULT 0,
  success TINYINT NOT NULL DEFAULT 1,
  error_message TEXT DEFAULT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS contract_contract;
CREATE TABLE contract_contract (
  id BIGINT NOT NULL AUTO_INCREMENT,
  contract_no VARCHAR(100) NOT NULL,
  contract_name VARCHAR(500) NOT NULL,
  contract_type VARCHAR(50) NOT NULL,
  party_a VARCHAR(200) NOT NULL,
  party_b VARCHAR(200) NOT NULL,
  amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  sign_date DATE DEFAULT NULL,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  file_url VARCHAR(500) DEFAULT '',
  file_type VARCHAR(20) DEFAULT '',
  file_size BIGINT DEFAULT 0,
  status VARCHAR(20) NOT NULL,
  risk_level VARCHAR(20) DEFAULT NULL,
  risk_score INT DEFAULT NULL,
  ai_review_time DATETIME DEFAULT NULL,
  reviewer_id BIGINT DEFAULT NULL,
  reviewer_name VARCHAR(100) DEFAULT '',
  review_time DATETIME DEFAULT NULL,
  review_comment TEXT DEFAULT NULL,
  upload_user_id BIGINT NOT NULL,
  upload_user_name VARCHAR(100) NOT NULL,
  upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT contract_contract_uk_contract_no UNIQUE (contract_no)
);

DROP TABLE IF EXISTS contract_review_result;
CREATE TABLE contract_review_result (
  id BIGINT NOT NULL AUTO_INCREMENT,
  contract_id BIGINT NOT NULL,
  risk_level VARCHAR(20) NOT NULL,
  risk_score INT NOT NULL,
  total_risks INT NOT NULL DEFAULT 0,
  high_risks INT NOT NULL DEFAULT 0,
  medium_risks INT NOT NULL DEFAULT 0,
  low_risks INT NOT NULL DEFAULT 0,
  conclusion TEXT DEFAULT NULL,
  report_url VARCHAR(500) DEFAULT '',
  matching_count INT DEFAULT 0,
  modified_count INT DEFAULT 0,
  missing_count INT DEFAULT 0,
  added_count INT DEFAULT 0,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT contract_review_result_uk_contract_id UNIQUE (contract_id)
);

DROP TABLE IF EXISTS contract_risk_clause;
CREATE TABLE contract_risk_clause (
  id BIGINT NOT NULL AUTO_INCREMENT,
  contract_id BIGINT NOT NULL,
  review_result_id BIGINT NOT NULL,
  clause_no VARCHAR(50) DEFAULT '',
  clause_title VARCHAR(200) NOT NULL,
  clause_content TEXT DEFAULT NULL,
  page INT DEFAULT NULL,
  risk_level VARCHAR(20) NOT NULL,
  risk_type VARCHAR(100) NOT NULL,
  risk_description TEXT DEFAULT NULL,
  suggestion TEXT DEFAULT NULL,
  legal_basis VARCHAR(200) DEFAULT '',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS contract_comment;
CREATE TABLE contract_comment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  contract_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  user_name VARCHAR(100) NOT NULL,
  user_role VARCHAR(100) NOT NULL,
  comment_type VARCHAR(20) NOT NULL,
  content TEXT NOT NULL,
  page INT DEFAULT NULL,
  position VARCHAR(100) DEFAULT '',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  resolve_time DATETIME DEFAULT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS contract_archive;
CREATE TABLE contract_archive (
  id BIGINT NOT NULL AUTO_INCREMENT,
  contract_id BIGINT NOT NULL,
  contract_no VARCHAR(100) NOT NULL,
  contract_name VARCHAR(500) NOT NULL,
  contract_type VARCHAR(50) NOT NULL,
  party_a VARCHAR(200) NOT NULL,
  party_b VARCHAR(200) NOT NULL,
  amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  archive_code VARCHAR(100) NOT NULL,
  archive_location VARCHAR(200) DEFAULT '',
  archive_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  archive_user_id BIGINT NOT NULL,
  archive_user_name VARCHAR(100) NOT NULL,
  storage_type VARCHAR(20) NOT NULL DEFAULT 'ELECTRONIC',
  retention_period INT DEFAULT 0,
  expire_date DATE DEFAULT NULL,
  description VARCHAR(500) DEFAULT '',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT contract_archive_uk_contract_id UNIQUE (contract_id),
  CONSTRAINT contract_archive_uk_archive_code UNIQUE (archive_code)
);

-- 注意：原业务字段 version（VARCHAR，模板版本号如 v2.3）重命名为 template_version，
--       腾出 version 作为乐观锁字段（INT），与全局规范保持一致。
DROP TABLE IF EXISTS contract_template;
CREATE TABLE contract_template (
  id BIGINT NOT NULL AUTO_INCREMENT,
  template_no VARCHAR(100) NOT NULL,
  template_name VARCHAR(200) NOT NULL,
  contract_type VARCHAR(50) NOT NULL,
  template_version VARCHAR(20) NOT NULL,
  file_url VARCHAR(500) DEFAULT '',
  description VARCHAR(500) DEFAULT '',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_by VARCHAR(50) NOT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) DEFAULT '',
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT contract_template_uk_template_no UNIQUE (template_no)
);

DROP TABLE IF EXISTS contract_clause;
CREATE TABLE contract_clause (
  id BIGINT NOT NULL AUTO_INCREMENT,
  clause_code VARCHAR(100) NOT NULL,
  clause_name VARCHAR(200) NOT NULL,
  clause_category VARCHAR(50) NOT NULL,
  contract_type VARCHAR(50) DEFAULT '',
  content TEXT DEFAULT NULL,
  standard_content TEXT DEFAULT NULL,
  risk_level VARCHAR(20) DEFAULT 'LOW',
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  description VARCHAR(500) DEFAULT '',
  legal_basis VARCHAR(200) DEFAULT '',
  created_by VARCHAR(50) DEFAULT '',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) DEFAULT '',
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT contract_clause_uk_clause_code UNIQUE (clause_code)
);

DROP TABLE IF EXISTS contract_review_rule;
CREATE TABLE contract_review_rule (
  id BIGINT NOT NULL AUTO_INCREMENT,
  rule_no VARCHAR(100) NOT NULL,
  rule_name VARCHAR(200) NOT NULL,
  rule_type VARCHAR(20) NOT NULL,
  contract_type VARCHAR(50) DEFAULT '',
  clause_keyword VARCHAR(200) NOT NULL,
  rule_config TEXT DEFAULT NULL,
  risk_level VARCHAR(20) NOT NULL,
  priority INT NOT NULL DEFAULT 1,
  status TINYINT NOT NULL DEFAULT 1,
  description VARCHAR(500) DEFAULT '',
  created_by VARCHAR(50) NOT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) DEFAULT '',
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT contract_review_rule_uk_rule_no UNIQUE (rule_no)
);

DROP TABLE IF EXISTS contract_risk_level;
CREATE TABLE contract_risk_level (
  id BIGINT NOT NULL AUTO_INCREMENT,
  risk_code VARCHAR(20) NOT NULL,
  risk_name VARCHAR(50) NOT NULL,
  risk_color VARCHAR(20) DEFAULT '',
  score_min INT NOT NULL DEFAULT 0,
  score_max INT NOT NULL DEFAULT 100,
  description VARCHAR(500) DEFAULT '',
  handling_suggestion TEXT DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  sort_order INT NOT NULL DEFAULT 0,
  created_by VARCHAR(50) DEFAULT '',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) DEFAULT '',
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT contract_risk_level_uk_risk_code UNIQUE (risk_code)
);

DROP TABLE IF EXISTS process_config;
CREATE TABLE process_config (
  id BIGINT NOT NULL AUTO_INCREMENT,
  process_code VARCHAR(100) NOT NULL,
  process_name VARCHAR(200) NOT NULL,
  contract_type VARCHAR(50) DEFAULT '',
  description VARCHAR(500) DEFAULT '',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  sort_order INT NOT NULL DEFAULT 0,
  created_by VARCHAR(50) DEFAULT '',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) DEFAULT '',
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT process_config_uk_process_code UNIQUE (process_code)
);

DROP TABLE IF EXISTS process_node;
CREATE TABLE process_node (
  id BIGINT NOT NULL AUTO_INCREMENT,
  process_id BIGINT NOT NULL,
  node_name VARCHAR(100) NOT NULL,
  node_type VARCHAR(20) NOT NULL,
  assignee_type VARCHAR(20) NOT NULL,
  assignee_id BIGINT DEFAULT NULL,
  assignee_name VARCHAR(100) DEFAULT '',
  sort_order INT NOT NULL DEFAULT 0,
  timeout_days INT DEFAULT 0,
  is_required TINYINT NOT NULL DEFAULT 1,
  description VARCHAR(500) DEFAULT '',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS process_instance;
CREATE TABLE process_instance (
  id BIGINT NOT NULL AUTO_INCREMENT,
  instance_code VARCHAR(100) NOT NULL,
  process_id BIGINT NOT NULL,
  process_name VARCHAR(200) NOT NULL,
  contract_id BIGINT NOT NULL,
  contract_no VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
  current_node_id BIGINT DEFAULT NULL,
  current_node_name VARCHAR(100) DEFAULT '',
  start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  end_time DATETIME DEFAULT NULL,
  creator_id BIGINT NOT NULL,
  creator_name VARCHAR(100) NOT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT process_instance_uk_instance_code UNIQUE (instance_code)
);

DROP TABLE IF EXISTS process_task;
CREATE TABLE process_task (
  id BIGINT NOT NULL AUTO_INCREMENT,
  task_code VARCHAR(100) NOT NULL,
  instance_id BIGINT NOT NULL,
  process_id BIGINT NOT NULL,
  node_id BIGINT NOT NULL,
  node_name VARCHAR(100) NOT NULL,
  node_type VARCHAR(20) NOT NULL,
  contract_id BIGINT NOT NULL,
  contract_no VARCHAR(100) NOT NULL,
  assignee_id BIGINT NOT NULL,
  assignee_name VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
  title VARCHAR(200) DEFAULT '',
  description TEXT DEFAULT NULL,
  deadline DATETIME DEFAULT NULL,
  start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  complete_time DATETIME DEFAULT NULL,
  result VARCHAR(20) DEFAULT NULL,
  comment TEXT DEFAULT NULL,
  creator_id BIGINT NOT NULL,
  creator_name VARCHAR(100) NOT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT process_task_uk_task_code UNIQUE (task_code)
);

-- 注：process_task_assign 含 assign_type/status 业务字段，按单表处理（非纯中间表），
--     原仅有 created_time，补齐 updated_time + deleted + version。
DROP TABLE IF EXISTS process_task_assign;
CREATE TABLE process_task_assign (
  id BIGINT NOT NULL AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  user_name VARCHAR(100) NOT NULL,
  assign_type VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT process_task_assign_uk_task_user UNIQUE (task_id, user_id)
);

DROP TABLE IF EXISTS integration_oa;
CREATE TABLE integration_oa (
  id BIGINT NOT NULL AUTO_INCREMENT,
  oa_type VARCHAR(50) NOT NULL,
  oa_name VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'CONNECTED',
  app_id VARCHAR(200) DEFAULT '',
  app_secret VARCHAR(200) DEFAULT '',
  access_token VARCHAR(500) DEFAULT '',
  token_expire_time DATETIME DEFAULT NULL,
  sync_status VARCHAR(20) NOT NULL DEFAULT 'DISABLED',
  last_sync_time DATETIME DEFAULT NULL,
  description VARCHAR(500) DEFAULT '',
  created_by VARCHAR(50) DEFAULT '',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) DEFAULT '',
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- append-only 日志表：integration_oa_log —— 不加 deleted/version
DROP TABLE IF EXISTS integration_oa_log;
CREATE TABLE integration_oa_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  oa_id BIGINT NOT NULL,
  log_type VARCHAR(50) NOT NULL,
  action VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
  request_data TEXT DEFAULT NULL,
  response_data TEXT DEFAULT NULL,
  error_message TEXT DEFAULT NULL,
  execution_time INT DEFAULT 0,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS integration_sign;
CREATE TABLE integration_sign (
  id BIGINT NOT NULL AUTO_INCREMENT,
  sign_type VARCHAR(50) NOT NULL,
  sign_name VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  app_id VARCHAR(200) DEFAULT '',
  app_key VARCHAR(200) DEFAULT '',
  certificate_path VARCHAR(500) DEFAULT '',
  certificate_number VARCHAR(200) DEFAULT '',
  certificate_expire_date DATE DEFAULT NULL,
  description VARCHAR(500) DEFAULT '',
  created_by VARCHAR(50) DEFAULT '',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) DEFAULT '',
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- append-only 记录表：integration_sign_record —— 不加 deleted/version
DROP TABLE IF EXISTS integration_sign_record;
CREATE TABLE integration_sign_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  sign_id BIGINT NOT NULL,
  contract_id BIGINT NOT NULL,
  contract_no VARCHAR(100) NOT NULL,
  sign_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  sign_user_id BIGINT NOT NULL,
  sign_user_name VARCHAR(100) NOT NULL,
  sign_position VARCHAR(100) DEFAULT '',
  signature_image VARCHAR(500) DEFAULT '',
  verify_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  verify_time DATETIME DEFAULT NULL,
  verify_result TEXT DEFAULT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS integration_api;
CREATE TABLE integration_api (
  id BIGINT NOT NULL AUTO_INCREMENT,
  api_name VARCHAR(100) NOT NULL,
  api_path VARCHAR(255) NOT NULL,
  http_method VARCHAR(10) NOT NULL,
  api_version VARCHAR(20) NOT NULL DEFAULT '1.0',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  auth_type VARCHAR(20) NOT NULL DEFAULT 'NONE',
  auth_config TEXT DEFAULT NULL,
  rate_limit INT DEFAULT 0,
  description VARCHAR(500) DEFAULT '',
  created_by VARCHAR(50) DEFAULT '',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) DEFAULT '',
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- append-only 日志表：integration_api_log —— 不加 deleted/version
DROP TABLE IF EXISTS integration_api_log;
CREATE TABLE integration_api_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  api_id BIGINT NOT NULL,
  api_name VARCHAR(100) NOT NULL,
  api_path VARCHAR(255) NOT NULL,
  http_method VARCHAR(10) NOT NULL,
  request_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  request_ip VARCHAR(50) DEFAULT '',
  request_param TEXT DEFAULT NULL,
  response_code INT DEFAULT 0,
  response_data TEXT DEFAULT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
  error_message TEXT DEFAULT NULL,
  execution_time INT DEFAULT 0,
  PRIMARY KEY (id)
);



-- 菜单 id 方案与前端 mock（mock.js / server.cjs）完全对齐，sys_role_menu 同步使用本 id 方案

-- sys_role_menu 授权与 server.cjs mockRoleMenus 完全对齐（使用上方菜单 id 方案）



-- 业务版本号字段已由 version 重命名为 template_version
