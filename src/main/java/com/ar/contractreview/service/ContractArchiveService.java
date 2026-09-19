package com.ar.contractreview.service;

import com.ar.contractreview.entity.ContractArchive;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 合同归档表 服务类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
public interface ContractArchiveService extends IService<ContractArchive> {
    IPage<ContractArchive> pageArchive(Integer page, Integer size, String contractNo, String contractName,
                                       String contractType, String archiveCode, String storageType);
}