package com.ar.contractreview.service.impl;

import com.ar.contractreview.entity.ContractRiskClause;
import com.ar.contractreview.exception.BusinessException;
import com.ar.contractreview.mapper.ContractRiskClauseMapper;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.service.ContractRiskClauseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * 风险条款表 服务实现类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Service
public class ContractRiskClauseServiceImpl extends ServiceImpl<ContractRiskClauseMapper, ContractRiskClause>
        implements ContractRiskClauseService {

    private static final Logger log = LoggerFactory.getLogger(ContractRiskClauseServiceImpl.class);

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> confirm(Long id, String comment) {
        return handle(id, "ACCEPTED", comment, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> reject(Long id, String comment) {
        return handle(id, "REJECTED", comment, true);
    }

    /**
     * 风险处理公共逻辑。
     * <p>
     * ⚠️ contract_risk_clause 表没有「处理备注」列，因此 comment 不入库，
     * 只写日志并在响应里回显。若后续要持久化，需要给表加列（涉及 MySQL 迁移）后再改这里。
     * </p>
     *
     * @param id           风险条款ID
     * @param targetStatus 目标状态 ACCEPTED / REJECTED
     * @param comment      处理备注
     * @param commentRequired 备注是否必填（驳回必填）
     */
    private Map<String, Object> handle(Long id, String targetStatus, String comment, boolean commentRequired) {
        ContractRiskClause risk = this.getById(id);
        if (risk == null) {
            throw new BusinessException(ResponseCode.DATA_NOT_EXIST.getCode(), "风险条款不存在");
        }
        if (commentRequired && !StringUtils.hasText(comment)) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "驳回理由是必填项");
        }
        if (!"PENDING".equals(risk.getStatus())) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "该风险已处理，不能重复操作");
        }

        LocalDateTime handleTime = LocalDateTime.now();
        risk.setStatus(targetStatus).setUpdatedTime(handleTime);
        this.updateById(risk);

        log.info("风险条款处理：id={}, 状态={}, 备注={}", id, targetStatus,
                StringUtils.hasText(comment) ? comment : "(无)");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("status", targetStatus);
        data.put("handleTime", handleTime.format(TIME_FMT));
        data.put("comment", comment);
        return data;
    }
}
