package com.ar.contractreview.service.impl;

import com.ar.contractreview.entity.ContractArchive;
import com.ar.contractreview.mapper.ContractArchiveMapper;
import com.ar.contractreview.service.ContractArchiveService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 合同归档表 服务实现类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Service
public class ContractArchiveServiceImpl extends ServiceImpl<ContractArchiveMapper, ContractArchive>
        implements ContractArchiveService {

    /**
     * 分页查询合同归档记录
     * <p>
     * 根据传入的筛选条件动态构建查询条件（仅当条件非空时才参与查询），
     * 并按归档时间倒序排列，最终返回分页结果。
     * </p>
     *
     * @param page         当前页码，从 1 开始
     * @param size         每页显示条数
     * @param contractNo   合同编号（可选，模糊匹配）
     * @param contractName 合同名称（可选，模糊匹配）
     * @param contractType 合同类型（可选，精确匹配）
     * @param archiveCode  归档编号（可选，模糊匹配）
     * @param storageType  存储方式（可选，精确匹配）
     * @return 合同归档记录的分页对象 {@link IPage}，包含当前页数据及分页元信息
     */
    @Override
    public IPage<ContractArchive> pageArchive(Integer page, Integer size, String contractNo, String contractName,
                                              String contractType, String archiveCode, String storageType) {
        // 构建 Lambda 查询条件包装器
        LambdaQueryWrapper<ContractArchive> qw = new LambdaQueryWrapper<>();
        qw
                // 合同编号：非空时进行模糊查询
                .like(StringUtils.hasText(contractNo), ContractArchive::getContractNo, contractNo)
                // 合同名称：非空时进行模糊查询
                .like(StringUtils.hasText(contractName), ContractArchive::getContractName, contractName)
                // 合同类型：非空时进行精确匹配
                .eq(StringUtils.hasText(contractType), ContractArchive::getContractType, contractType)
                // 归档编号：非空时进行模糊查询
                .like(StringUtils.hasText(archiveCode), ContractArchive::getArchiveCode, archiveCode)
                // 存储方式：非空时进行精确匹配
                .eq(StringUtils.hasText(storageType), ContractArchive::getStorageType, storageType)
                // 按归档时间倒序排列，最新的归档记录排在最前
                .orderByDesc(ContractArchive::getArchiveTime);
        // 执行分页查询并返回结果
        return this.page(new Page<>(page, size), qw);
    }
}