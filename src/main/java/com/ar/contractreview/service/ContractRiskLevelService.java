package com.ar.contractreview.service;

import com.ar.contractreview.entity.ContractRiskLevel;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 风险等级配置表 服务类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
public interface ContractRiskLevelService extends IService<ContractRiskLevel> {
    /**
     * 12.1 获取风险等级列表
     * @return 风险等级列表
     */
    List<ContractRiskLevel> listRiskLevels();

    /**
     * 12.2 获取风险等级详情
     * @param id 风险等级ID
     * @return 风险等级
     */
    ContractRiskLevel getRiskLevelById(Long id);

    /**
     * 12.3 新增风险等级
     * @param riskLevel 风险等级信息
     * @return 新增后的风险等级
     */
    ContractRiskLevel addRiskLevel(ContractRiskLevel riskLevel);

    /**
     * 12.4 更新风险等级
     * @param riskLevel 风险等级信息
     * @return 更新后的风险等级
     */
    ContractRiskLevel updateRiskLevel(ContractRiskLevel riskLevel);

    /**
     * 12.5 删除风险等级
     * @param id 风险等级ID
     * @return 是否成功
     */
    boolean deleteRiskLevel(Long id);

}
