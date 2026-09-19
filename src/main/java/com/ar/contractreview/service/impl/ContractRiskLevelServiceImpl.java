package com.ar.contractreview.service.impl;

import com.ar.contractreview.entity.ContractRiskLevel;
import com.ar.contractreview.mapper.ContractRiskLevelMapper;
import com.ar.contractreview.service.ContractRiskLevelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 风险等级配置表 服务实现类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Service
public class ContractRiskLevelServiceImpl extends ServiceImpl<ContractRiskLevelMapper, ContractRiskLevel> implements ContractRiskLevelService {
    @Autowired
    private ContractRiskLevelMapper contractRiskLevelMapper;

    @Override
    public List<ContractRiskLevel> listRiskLevels() {
        LambdaQueryWrapper<ContractRiskLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ContractRiskLevel::getSortOrder);
        return contractRiskLevelMapper.selectList(wrapper);
    }

    @Override
    public ContractRiskLevel getRiskLevelById(Long id) {
        return contractRiskLevelMapper.selectById(id);
    }

    @Override
    public ContractRiskLevel addRiskLevel(ContractRiskLevel riskLevel) {
        if (null == riskLevel.getSortOrder()) {
            // 查询当前最大排序号，新加的在最后
            LambdaQueryWrapper<ContractRiskLevel> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(ContractRiskLevel::getSortOrder);
            wrapper.last("LIMIT 1");
            ContractRiskLevel max = contractRiskLevelMapper.selectOne(wrapper);
            riskLevel.setSortOrder((max == null ? 0 : max.getSortOrder()) + 1);
        }
        if (null == riskLevel.getStatus()) {
            riskLevel.setStatus((byte) 1);
        }
        riskLevel.setCreatedTime(LocalDateTime.now());
        this.save(riskLevel);
        return riskLevel;
    }

    @Override
    public ContractRiskLevel updateRiskLevel(ContractRiskLevel riskLevel) {
        riskLevel.setUpdatedTime(LocalDateTime.now());
        this.updateById(riskLevel);
        return this.getById(riskLevel.getId());
    }

    @Override
    public boolean deleteRiskLevel(Long id) {
        return this.removeById(id);
    }

}
