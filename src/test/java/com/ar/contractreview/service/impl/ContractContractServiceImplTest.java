package com.ar.contractreview.service.impl;

import com.ar.contractreview.entity.ContractArchive;
import com.ar.contractreview.entity.ContractContract;
import com.ar.contractreview.exception.BusinessException;
import com.ar.contractreview.mapper.ContractContractMapper;
import com.ar.contractreview.service.ContractArchiveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ContractContractServiceImpl 状态机守卫单元测试（无需数据库）。
 * 覆盖：approve/reject/archive/submitReview 的状态校验，以及归档事务逻辑。
 */
@ExtendWith(MockitoExtension.class)
class ContractContractServiceImplTest {

    @Mock
    private ContractContractMapper contractContractMapper;
    @Mock
    private ContractArchiveService contractArchiveService;

    private ContractContractServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ContractContractServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", contractContractMapper);
        ReflectionTestUtils.setField(service, "contractArchiveService", contractArchiveService);
    }

    private ContractContract contract(Long id, String status) {
        return new ContractContract().setId(id).setStatus(status);
    }

    @Test
    void approve_fromReviewing_shouldSucceed() {
        when(contractContractMapper.selectById(1L)).thenReturn(contract(1L, "REVIEWING"));
        when(contractContractMapper.updateById(any(ContractContract.class))).thenReturn(1);  // ✅ 修复

        ContractContract r = service.approve(1L, "通过");

        assertEquals("APPROVED", r.getStatus());
        verify(contractContractMapper).updateById(any(ContractContract.class));  // ✅ 修复
    }

    @Test
    void approve_fromUploaded_shouldThrow() {
        when(contractContractMapper.selectById(1L)).thenReturn(contract(1L, "UPLOADED"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.approve(1L, "x"));
        assertEquals(100, ex.getCode());
        verify(contractContractMapper, never()).updateById(any(ContractContract.class));  // ✅ 修复
    }

    @Test
    void reject_fromReviewing_shouldSucceed() {
        when(contractContractMapper.selectById(1L)).thenReturn(contract(1L, "REVIEWING"));
        when(contractContractMapper.updateById(any(ContractContract.class))).thenReturn(1);  // ✅ 修复

        ContractContract r = service.reject(1L, "驳回");

        assertEquals("REJECTED", r.getStatus());
        verify(contractContractMapper).updateById(any(ContractContract.class));  // ✅ 修复
    }

    @Test
    void reject_fromApproved_shouldThrow() {
        when(contractContractMapper.selectById(1L)).thenReturn(contract(1L, "APPROVED"));

        assertThrows(BusinessException.class, () -> service.reject(1L, "x"));
        verify(contractContractMapper, never()).updateById(any(ContractContract.class));  // ✅ 修复
    }

    @Test
    void submitReview_fromUploaded_shouldWork() {
        when(contractContractMapper.selectById(1L)).thenReturn(contract(1L, "UPLOADED"));
        when(contractContractMapper.updateById(any(ContractContract.class))).thenReturn(1);  // ✅ 修复

        ContractContract r = service.submitReview(1L, 9L, "请审核");

        assertEquals("REVIEWING", r.getStatus());
        assertEquals(9L, r.getReviewerId());
        verify(contractContractMapper).updateById(any(ContractContract.class));  // ✅ 修复
    }

    @Test
    void submitReview_fromApproved_shouldThrow() {
        when(contractContractMapper.selectById(1L)).thenReturn(contract(1L, "APPROVED"));

        assertThrows(BusinessException.class, () -> service.submitReview(1L, 9L, null));
        verify(contractContractMapper, never()).updateById(any(ContractContract.class));  // ✅ 修复
    }

    @Test
    void archive_fromApproved_shouldSaveArchive() {
        when(contractContractMapper.selectById(1L)).thenReturn(contract(1L, "APPROVED"));
        when(contractContractMapper.updateById(any(ContractContract.class))).thenReturn(1);  // ✅ 修复
        when(contractArchiveService.save(any(ContractArchive.class))).thenReturn(true);  // ✅ 也修复一下

        ContractArchive a = service.archive(1L, "ELECTRONIC", "A-1", 12);

        assertEquals("ELECTRONIC", a.getStorageType());
        assertEquals(1L, a.getContractId());
        verify(contractArchiveService).save(any(ContractArchive.class));  // ✅ 也修复一下
    }

    @Test
    void archive_fromUploaded_shouldThrow() {
        when(contractContractMapper.selectById(1L)).thenReturn(contract(1L, "UPLOADED"));

        assertThrows(BusinessException.class, () -> service.archive(1L, null, null, null));
        verify(contractArchiveService, never()).save(any(ContractArchive.class));  // ✅ 也修复一下
        verify(contractContractMapper, never()).updateById(any(ContractContract.class));  // ✅ 修复
    }
}