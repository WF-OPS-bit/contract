package com.ar.contractreview.service;

import com.ar.contractreview.entity.ContractReviewRule;
import com.ar.contractreview.security.dto.RuleSaveRequest;
import com.ar.contractreview.vo.RuleVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 审核规则表 服务类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
public interface ContractReviewRuleService extends IService<ContractReviewRule> {

    /**
     * 11.1 获取规则列表（分页，支持多条件筛选）
     *
     * @param page         页码
     * @param size         每页条数
     * @param ruleName     规则名称（模糊）
     * @param ruleType     规则类型
     * @param contractType 适用合同类型
     * @param riskLevel    风险等级
     * @param status       状态
     * @return 规则分页结果
     */
    IPage<RuleVO> pageRules(int page, int size, String ruleName, String ruleType,
                            String contractType, String riskLevel, Integer status);

    /**
     * 11.2 获取规则详情
     *
     * @param id 规则ID
     * @return 规则详情
     */
    RuleVO getRuleDetail(Long id);

    /**
     * 11.3 新增规则
     *
     * @param request 规则请求体
     * @return 新增后的规则
     */
    ContractReviewRule addRule(RuleSaveRequest request);

    /**
     * 11.4 更新规则
     *
     * @param id      规则ID
     * @param request 规则请求体
     * @return 更新后的规则
     */
    ContractReviewRule updateRule(Long id, RuleSaveRequest request);

    /**
     * 11.5 删除规则
     *
     * @param id 规则ID
     */
    void deleteRule(Long id);
}
