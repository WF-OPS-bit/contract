package com.ar.contractreview.service;

import com.ar.contractreview.entity.ContractRiskClause;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 风险条款表 服务类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
public interface ContractRiskClauseService extends IService<ContractRiskClause> {

    /**
     * 6.3 确认风险：把风险条款标记为已采纳（ACCEPTED）。
     *
     * @param id      风险条款ID
     * @param comment 确认备注（可选）
     * @return {id, status, handleTime}
     */
    Map<String, Object> confirm(Long id, String comment);

    /**
     * 6.4 驳回风险：把风险条款标记为已拒绝（REJECTED）。
     *
     * @param id      风险条款ID
     * @param comment 驳回理由（必填）
     * @return {id, status, handleTime}
     */
    Map<String, Object> reject(Long id, String comment);
}
