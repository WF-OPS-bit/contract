package com.ar.contractreview.controller;

import com.ar.contractreview.entity.ContractArchive;
import com.ar.contractreview.entity.ContractContract;
import com.ar.contractreview.result.R;
import com.ar.contractreview.service.ContractContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ContractContractController 校验逻辑单元测试（无需启动 Spring / 连接数据库）。
 * 覆盖：上传文件类型白名单、空文件拒绝、review 的 reviewerId 参数校验、以及 approve/archive 正常路径。
 */
@ExtendWith(MockitoExtension.class)
class ContractContractControllerTest {

    @Mock
    private ContractContractService contractContractService;

    @InjectMocks
    private ContractContractController controller;

    @BeforeEach
    void setUp() {
        // 上传落地到系统临时目录，避免依赖 D:/ 盘，保证测试在任何机器可运行
        System.setProperty("contract.upload.dir",
                System.getProperty("java.io.tmpdir") + File.separator + "contract-files");
    }

    @Test
    void upload_unsupportedExtension_shouldReject() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "evil.exe", "application/octet-stream", "x".getBytes());

        R r = controller.upload(file, "n", "t", "a", "b", new BigDecimal("100"), "2026-01-01", null, null);

        assertEquals(100, r.getCode());
        verify(contractContractService, never()).save(any());
    }

    @Test
    void upload_emptyFile_shouldReject() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        R r = controller.upload(file, "n", "t", "a", "b", new BigDecimal("100"), "2026-01-01", null, null);

        assertEquals(100, r.getCode());
        verify(contractContractService, never()).save(any());
    }

    @Test
    void upload_allowedPdf_shouldSave() throws Exception {
        when(contractContractService.save(any())).thenReturn(true);
        MockMultipartFile file =
                new MockMultipartFile("file", "contract.pdf", "application/pdf", "x".getBytes());

        R r = controller.upload(file, "n", "t", "a", "b", new BigDecimal("100"), "2026-01-01", null, null);

        assertEquals(200, r.getCode());
        verify(contractContractService).save(any());
    }

    @Test
    void review_missingReviewerId_shouldReject() {
        R r = controller.review(1L, Map.of("reviewComment", "请审核"));

        assertEquals(100, r.getCode());
        verify(contractContractService, never()).submitReview(any(), any(), any());
    }

    @Test
    void review_nonNumericReviewerId_shouldReject() {
        R r = controller.review(1L, Map.of("reviewerId", "abc"));

        assertEquals(100, r.getCode());
        verify(contractContractService, never()).submitReview(any(), any(), any());
    }

    @Test
    void approve_ok() {
        ContractContract c = new ContractContract().setId(1L).setStatus("APPROVED");
        when(contractContractService.approve(eq(1L), any())).thenReturn(c);

        R r = controller.approve(1L, Map.of("reviewComment", "ok"));

        assertEquals(200, r.getCode());
    }

    @Test
    void archive_ok() {
        ContractArchive a = new ContractArchive().setContractId(1L).setArchiveCode("ARC1");
        when(contractContractService.archive(eq(1L), any(), any(), any())).thenReturn(a);

        R r = controller.archive(1L, Map.of("storageType", "ELECTRONIC"));

        assertEquals(200, r.getCode());
    }
}
