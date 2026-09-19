package com.ar.contractreview.service.impl;

import com.ar.contractreview.entity.ContractComment;
import com.ar.contractreview.mapper.ContractCommentMapper;
import com.ar.contractreview.service.ContractCommentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 合同批注表 服务实现类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Service
public class ContractCommentServiceImpl extends ServiceImpl<ContractCommentMapper, ContractComment>
        implements ContractCommentService {

    /**
     * 根据合同ID查询该合同下所有未删除的批注信息
     * 结果按批注的创建时间进行升序排列（即最早创建的排在最前面）
     *
     * @param contractId 合同ID
     * @return 符合条件的批注列表
     */
    @Override
    public List<ContractComment> listByContract(Long contractId) {
        // 使用 MyBatis-Plus 的 LambdaQueryWrapper 构建类型安全的查询条件
        return this.list(new LambdaQueryWrapper<ContractComment>()
                // 1. 匹配合同ID：只查询属于该指定合同的数据
                .eq(ContractComment::getContractId, contractId)
                // 2. 过滤逻辑删除：状态不等于 "DELETED"（保留有效数据）
                .ne(ContractComment::getStatus, "DELETED")
                // 3. 排序规则：按创建时间升序排列（时间早的在前）
                .orderByAsc(ContractComment::getCreatedTime));
    }
}