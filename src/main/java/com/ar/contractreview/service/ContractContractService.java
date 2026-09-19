package com.ar.contractreview.service;

import com.ar.contractreview.entity.ContractArchive;
import com.ar.contractreview.entity.ContractContract;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 合同表 服务类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
public interface ContractContractService extends IService<ContractContract> {
    IPage<ContractContract> pageContract(Integer page, Integer size, String contractNo, String contractName,
                                         String contractType, String status, String riskLevel, Long uploadUserId,
                                         String startTime, String endTime);
    ContractContract submitReview(Long id, Long reviewerId, String comment);
    ContractContract approve(Long id, String comment);
    ContractContract reject(Long id, String comment);
    ContractArchive archive(Long id, String storageType, String archiveLocation, Integer retentionPeriod);
}
