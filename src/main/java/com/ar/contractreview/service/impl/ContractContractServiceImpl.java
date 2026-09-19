package com.ar.contractreview.service.impl;

import com.ar.contractreview.entity.ContractArchive;
import com.ar.contractreview.entity.ContractContract;
import com.ar.contractreview.exception.BusinessException;
import com.ar.contractreview.mapper.ContractContractMapper;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.service.ContractArchiveService;
import com.ar.contractreview.service.ContractContractService;
import com.ar.contractreview.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * <p>
 * 合同表 服务实现类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Service
public class ContractContractServiceImpl extends ServiceImpl<ContractContractMapper, ContractContract>
        implements ContractContractService {

    @Autowired
    private ContractArchiveService contractArchiveService;

    @Override public IPage<ContractContract> pageContract(Integer page, Integer size, String contractNo, String contractName,
                                                          String contractType, String status, String riskLevel, Long uploadUserId, String startTime, String endTime) {
        LambdaQueryWrapper<ContractContract> qw = new LambdaQueryWrapper<>();
        qw.like(StringUtils.hasText(contractNo), ContractContract::getContractNo, contractNo)
                .like(StringUtils.hasText(contractName), ContractContract::getContractName, contractName)
                .eq(StringUtils.hasText(contractType), ContractContract::getContractType, contractType)
                .eq(StringUtils.hasText(status), ContractContract::getStatus, status)
                .eq(StringUtils.hasText(riskLevel), ContractContract::getRiskLevel, riskLevel)
                .eq(uploadUserId != null, ContractContract::getUploadUserId, uploadUserId)
                .apply(StringUtils.hasText(startTime), "upload_time >= {0}", startTime)
                .apply(StringUtils.hasText(endTime), "upload_time <= {0}", endTime)
                .orderByDesc(ContractContract::getUploadTime);
        return this.page(new Page<>(page, size), qw);
    }

    @Override
    public ContractContract submitReview(Long id, Long reviewerId, String comment) {
        ContractContract c = this.getById(id);
        if (c == null) throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION);
        // 状态守卫：仅“已上传/已驳回”可提交审核（被驳回后可重新提交）
        if (!"UPLOADED".equals(c.getStatus()) && !"REJECTED".equals(c.getStatus())) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "当前状态不可提交审核");
        }
        c.setStatus("REVIEWING").setReviewerId(reviewerId).setReviewComment(comment);
        this.updateById(c);
        return c;
    }

    @Override
    public ContractContract approve(Long id, String comment) {
        ContractContract c = this.getById(id);
        if (c == null) throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION);
        // 状态守卫：仅“审核中”可审核通过，防止任意状态直接置为已通过
        if (!"REVIEWING".equals(c.getStatus())) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "当前状态不可审核通过");
        }
        Long operatorId = SecurityUtils.currentUserId();
        // 越权守卫：若合同已指派审核人，则只允许该审核人操作（当前登录人缺失/未指派时跳过校验）
        if (c.getReviewerId() != null && operatorId != null && !c.getReviewerId().equals(operatorId)) {
            throw new BusinessException(ResponseCode.NO_PERM_EXCEPTION.getCode(), "审核人与当前登录人不一致");
        }
        c.setStatus("APPROVED")
                .setReviewerId(operatorId != null ? operatorId : c.getReviewerId())
                .setReviewerName(SecurityUtils.currentUsername())
                .setReviewComment(comment)
                .setReviewTime(LocalDateTime.now());
        this.updateById(c);
        return c;
    }

    @Override
    public ContractContract reject(Long id, String comment) {
        ContractContract c = this.getById(id);
        if (c == null) throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION);
        // 状态守卫：仅“审核中”可驳回
        if (!"REVIEWING".equals(c.getStatus())) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "当前状态不可驳回");
        }
        Long operatorId = SecurityUtils.currentUserId();
        if (c.getReviewerId() != null && operatorId != null && !c.getReviewerId().equals(operatorId)) {
            throw new BusinessException(ResponseCode.NO_PERM_EXCEPTION.getCode(), "审核人与当前登录人不一致");
        }
        c.setStatus("REJECTED")
                .setReviewerId(operatorId != null ? operatorId : c.getReviewerId())
                .setReviewerName(SecurityUtils.currentUsername())
                .setReviewComment(comment)
                .setReviewTime(LocalDateTime.now());
        this.updateById(c);
        return c;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContractArchive archive(Long id, String storageType, String archiveLocation, Integer retentionPeriod) {
        ContractContract c = this.getById(id);
        if (c == null) throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION);
        // 状态守卫：仅“已通过”的合同可归档
        if (!"APPROVED".equals(c.getStatus())) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "只有已通过的合同才能归档");
        }
        c.setStatus("ARCHIVED");
        this.updateById(c);

        ContractArchive a = new ContractArchive()
                .setContractId(c.getId()).setContractNo(c.getContractNo()).setContractName(c.getContractName())
                .setContractType(c.getContractType()).setPartyA(c.getPartyA()).setPartyB(c.getPartyB()).setAmount(c.getAmount())
                .setArchiveCode("ARC" + System.currentTimeMillis())
                .setArchiveLocation(archiveLocation)
                .setArchiveTime(LocalDateTime.now())
                .setArchiveUserId(SecurityUtils.currentUserId())
                .setArchiveUserName(SecurityUtils.currentUsername())
                .setStorageType(storageType != null ? storageType : "ELECTRONIC")
                .setRetentionPeriod(retentionPeriod)
                .setCreatedTime(LocalDateTime.now()).setUpdatedTime(LocalDateTime.now());
        contractArchiveService.save(a);
        return a;
    }
}
