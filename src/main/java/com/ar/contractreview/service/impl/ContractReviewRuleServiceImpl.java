package com.ar.contractreview.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ar.contractreview.entity.ContractReviewRule;
import com.ar.contractreview.exception.BusinessException;
import com.ar.contractreview.mapper.ContractReviewRuleMapper;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.security.dto.RuleSaveRequest;
import com.ar.contractreview.service.ContractReviewRuleService;
import com.ar.contractreview.vo.RuleVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 审核规则表 服务实现类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Service
public class ContractReviewRuleServiceImpl extends ServiceImpl<ContractReviewRuleMapper, ContractReviewRule>
        implements ContractReviewRuleService {

    @Override
    public IPage<RuleVO> pageRules(int page, int size, String ruleName, String ruleType,
                                   String contractType, String riskLevel, Integer status) {
        Page<ContractReviewRule> pageParam = new Page<>(page, size);
        Page<ContractReviewRule> result = page(pageParam,
                Wrappers.<ContractReviewRule>lambdaQuery()
                        .like(ruleName != null && !ruleName.trim().isEmpty(),
                                ContractReviewRule::getRuleName, ruleName)
                        .eq(ruleType != null && !ruleType.trim().isEmpty(),
                                ContractReviewRule::getRuleType, ruleType)
                        .eq(contractType != null && !contractType.trim().isEmpty(),
                                ContractReviewRule::getContractType, contractType)
                        .eq(riskLevel != null && !riskLevel.trim().isEmpty(),
                                ContractReviewRule::getRiskLevel, riskLevel)
                        .eq(status != null, ContractReviewRule::getStatus, status)
        );

        IPage<RuleVO> voPage = new Page<>(page, size, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public RuleVO getRuleDetail(Long id) {
        return toVO(requireRule(id));
    }

    @Override
    public ContractReviewRule addRule(RuleSaveRequest request) {
        validateSaveRequest(request);
        // 规则编号唯一性校验
        checkRuleNoUnique(request.getRuleNo().trim(), null);

        String operator = currentUserName();
        LocalDateTime now = LocalDateTime.now();
        ContractReviewRule rule = new ContractReviewRule();
        rule.setRuleNo(request.getRuleNo().trim());
        rule.setRuleName(request.getRuleName().trim());
        rule.setRuleType(request.getRuleType().trim());
        rule.setContractType(request.getContractType());
        rule.setClauseKeyword(request.getClauseKeyword().trim());
        rule.setRuleConfig(toConfigString(request.getRuleConfig()));
        rule.setRiskLevel(request.getRiskLevel().trim());
        rule.setPriority(request.getPriority() == null ? 1 : request.getPriority());
        rule.setStatus((byte) 1);
        rule.setDescription(request.getDescription());
        rule.setCreatedBy(operator);
        rule.setCreatedTime(now);
        rule.setUpdatedBy(operator);
        rule.setUpdatedTime(now);
        rule.setDeleted((byte) 0);
        boolean saved = save(rule);
        if (!saved) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "新增规则失败");
        }
        return rule;
    }

    @Override
    public ContractReviewRule updateRule(Long id, RuleSaveRequest request) {
        ContractReviewRule rule = requireRule(id);
        validateSaveRequest(request);
        // 规则编号唯一性校验（排除自身）
        checkRuleNoUnique(request.getRuleNo().trim(), id);

        rule.setRuleNo(request.getRuleNo().trim());
        rule.setRuleName(request.getRuleName().trim());
        rule.setRuleType(request.getRuleType().trim());
        rule.setContractType(request.getContractType());
        rule.setClauseKeyword(request.getClauseKeyword().trim());
        rule.setRuleConfig(toConfigString(request.getRuleConfig()));
        rule.setRiskLevel(request.getRiskLevel().trim());
        if (request.getPriority() != null) {
            rule.setPriority(request.getPriority());
        }
        rule.setDescription(request.getDescription());
        rule.setUpdatedBy(currentUserName());
        rule.setUpdatedTime(LocalDateTime.now());
        boolean updated = updateById(rule);
        if (!updated) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "更新规则失败");
        }
        return rule;
    }

    @Override
    public void deleteRule(Long id) {
        requireRule(id);
        boolean removed = removeById(id);
        if (!removed) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "删除规则失败");
        }
    }

    /**
     * 查询规则，不存在则抛出业务异常
     */
    private ContractReviewRule requireRule(Long id) {
        if (id == null) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "规则ID不能为空");
        }
        ContractReviewRule rule = getById(id);
        if (rule == null) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "规则不存在");
        }
        return rule;
    }

    /**
     * 校验新增/更新规则的公共参数
     */
    private void validateSaveRequest(RuleSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "请求参数不能为空");
        }
        if (request.getRuleName() == null || request.getRuleName().trim().isEmpty()) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "规则名称不能为空");
        }
        if (request.getRuleNo() == null || request.getRuleNo().trim().isEmpty()) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "规则编号不能为空");
        }
        if (request.getRuleType() == null || request.getRuleType().trim().isEmpty()) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "规则类型不能为空");
        }
        if (request.getClauseKeyword() == null || request.getClauseKeyword().trim().isEmpty()) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "条款关键词不能为空");
        }
        if (request.getRiskLevel() == null || request.getRiskLevel().trim().isEmpty()) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "风险等级不能为空");
        }
    }

    /**
     * 规则编号唯一性校验
     *
     * @param ruleNo    规则编号
     * @param excludeId 需要排除的规则ID（更新时传，新增传 null）
     */
    private void checkRuleNoUnique(String ruleNo, Long excludeId) {
        ContractReviewRule exist = getOne(
                Wrappers.<ContractReviewRule>lambdaQuery().eq(ContractReviewRule::getRuleNo, ruleNo)
        );
        if (exist != null && !exist.getId().equals(excludeId)) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "规则编号已存在：" + ruleNo);
        }
    }

    /**
     * 规则配置对象转存储字符串：null 返回 null，否则序列化为 JSON 字符串
     */
    private String toConfigString(Object ruleConfig) {
        if (ruleConfig == null) {
            return null;
        }
        return JSON.toJSONString(ruleConfig);
    }

    /**
     * 实体转响应对象：ruleConfig 由存储字符串解析回 JSON 对象
     */
    private RuleVO toVO(ContractReviewRule rule) {
        RuleVO vo = new RuleVO();
        vo.setId(rule.getId());
        vo.setRuleNo(rule.getRuleNo());
        vo.setRuleName(rule.getRuleName());
        vo.setRuleType(rule.getRuleType());
        vo.setContractType(rule.getContractType());
        vo.setClauseKeyword(rule.getClauseKeyword());
        vo.setRuleConfig(parseConfig(rule.getRuleConfig()));
        vo.setRiskLevel(rule.getRiskLevel());
        vo.setPriority(rule.getPriority());
        vo.setStatus(rule.getStatus());
        vo.setDescription(rule.getDescription());
        return vo;
    }

    /**
     * 存储的规则配置字符串解析为 JSON 对象；空字符串或 null 返回 null
     */
    private Object parseConfig(String ruleConfig) {
        if (ruleConfig == null || ruleConfig.trim().isEmpty()) {
            return null;
        }
        try {
            return JSON.parse(ruleConfig);
        } catch (Exception e) {
            // 存储值非法时原样返回，避免接口异常
            return ruleConfig;
        }
    }

    /**
     * 获取当前登录用户名，未登录（内部调用等场景）时返回 system
     */
    private String currentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.ar.contractreview.security.bo.SecurityUser securityUser) {
            return securityUser.getUsername();
        }
        return "system";
    }
}
