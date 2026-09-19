package com.ar.contractreview.service;

import com.ar.contractreview.entity.ContractComment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 合同批注表 服务类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
public interface ContractCommentService extends IService<ContractComment> {

    /**
     * 根据合同ID查询该合同下所有未删除的批注信息
     * 结果按批注的创建时间进行升序排列
     *
     * @param contractId 合同ID
     * @return 符合条件的批注列表
     */
    List<ContractComment> listByContract(Long contractId);
}