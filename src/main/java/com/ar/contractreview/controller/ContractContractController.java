package com.ar.contractreview.controller;

import com.ar.contractreview.entity.ContractArchive;
import com.ar.contractreview.entity.ContractContract;
import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.service.ContractContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * <p>
 * 合同表 前端控制器
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/contracts")
public class ContractContractController {

    /** 上传文件大小上限：10MB */
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    /** 仅允许的文档/图片扩展名（白名单，小写） */
    private static final Set<String> ALLOWED_EXT =
            Set.of("pdf", "doc", "docx", "xls", "xlsx", "txt", "jpg", "png");
    /**
     * 上传目录：默认 D:/contract-files/，可通过 JVM 参数 -Dcontract.upload.dir=... 覆盖（便于测试/部署）。
     */
    private String uploadDir() {
        return System.getProperty("contract.upload.dir", "D:/contract-files/");
    }

    @Autowired
    private ContractContractService contractContractService;

    private R paramFail(String msg) {
        return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), msg);
    }

    // 5.1 上传合同
    @PostMapping("/upload")
    public R upload(@RequestParam("file") MultipartFile file,
                    @RequestParam("contractName") String contractName, @RequestParam("contractType") String contractType,
                    @RequestParam("partyA") String partyA, @RequestParam("partyB") String partyB,
                    @RequestParam("amount") BigDecimal amount,
                    @RequestParam("signDate") String signDate,
                    @RequestParam(value = "startDate", required = false) String startDate,
                    @RequestParam(value = "endDate", required = false) String endDate) throws Exception {
        if (file == null || file.isEmpty()) return paramFail("请选择文件");
        if (file.getSize() > MAX_FILE_SIZE) return paramFail("文件不能超过 10MB");

        // 文件名完全重命名(UUID + 白名单扩展名)，不信任客户端原始文件名，杜绝 ../ 目录穿越与任意类型上传
        String ext = extractExt(file.getOriginalFilename());
        if (ext == null || !ALLOWED_EXT.contains(ext)) return paramFail("不支持的文件类型");

        java.io.File dir = new java.io.File(uploadDir());
        if (!dir.exists() && !dir.mkdirs()) return paramFail("上传目录创建失败");
        String filename = UUID.randomUUID() + "." + ext;
        java.io.File dest = new java.io.File(dir, filename);
        file.transferTo(dest);

        ContractContract c = new ContractContract()
                .setContractNo("CT" + LocalDate.now().toString().replace("-", "") + System.currentTimeMillis())
                .setContractName(contractName).setContractType(contractType)
                .setPartyA(partyA).setPartyB(partyB).setAmount(amount)
                .setFileUrl("/files/contracts/" + filename).setFileType(ext).setFileSize(file.getSize())
                .setStatus("UPLOADED")
                .setUploadUserId(com.ar.contractreview.utils.SecurityUtils.currentUserId())
                .setUploadUserName(com.ar.contractreview.utils.SecurityUtils.currentUsername())
                .setUploadTime(LocalDateTime.now())
                .setCreatedTime(LocalDateTime.now()).setUpdatedTime(LocalDateTime.now());
        if (StringUtils.hasText(signDate)) c.setSignDate(LocalDate.parse(signDate));
        if (StringUtils.hasText(startDate)) c.setStartDate(LocalDate.parse(startDate));
        if (StringUtils.hasText(endDate)) c.setEndDate(LocalDate.parse(endDate));

        try {
            contractContractService.save(c);
        } catch (Exception e) {
            dest.delete();   // 落库失败时清理已写盘的孤儿文件，避免磁盘与数据库不一致
            throw e;
        }
        return R.ok().data("contract", c);
    }

    // 5.2 合同列表
    @GetMapping
    public R list(@RequestParam(value="page",defaultValue = "1") Integer page, @RequestParam(value = "size",defaultValue = "20") Integer size,
                  @RequestParam(value="contractNo",required = false) String contractNo, @RequestParam(value = "contractName",required = false) String contractName,
                  @RequestParam(value="contractType",required = false) String contractType, @RequestParam(value = "status",required = false) String status,
                  @RequestParam(value = "riskLevel",required = false) String riskLevel, @RequestParam(value = "uploadUserId",required = false) Long uploadUserId,
                  @RequestParam(value = "startTime",required = false) String startTime, @RequestParam(value = "endTime",required = false) String endTime) {
        var p = contractContractService.pageContract(page, size, contractNo, contractName, contractType, status, riskLevel, uploadUserId, startTime, endTime);
        return R.ok().data("list", p.getRecords()).data("total", p.getTotal()).data("page", p.getCurrent()).data("size", p.getSize());
    }

    // 5.3 合同详情
    @GetMapping("/{id}") public R detail(@PathVariable("id") Long id) {
        ContractContract c = contractContractService.getById(id);
        return c == null ? R.fail(ResponseCode.PARAMETER_EXCEPTION) : R.ok().data("contract", c);
    }

    // 5.4 删除合同
    @DeleteMapping("/{id}") public R delete(@PathVariable("id") Long id) { contractContractService.removeById(id); return R.ok().data("deleted", true); }

    // 5.5 提交审核
    @PostMapping("/{id}/review")
    public R review(@PathVariable("id") Long id, @RequestBody Map<String,Object> body) {
        Object rid = body == null ? null : body.get("reviewerId");
        if (rid == null) return paramFail("缺少审核人");
        Long reviewerId;
        try {
            reviewerId = Long.valueOf(String.valueOf(rid));
        } catch (NumberFormatException e) {
            return paramFail("审核人参数错误");
        }
        return R.ok().data("contract", contractContractService.submitReview(id, reviewerId, body == null ? null : (String) body.get("reviewComment")));
    }

    // 5.6 审核通过（状态守卫 + 当前登录人作为实际审核人，见 Service）
    @PostMapping("/{id}/approve")
    public R approve(@PathVariable("id") Long id, @RequestBody(required = false) Map<String,Object> body) {
        String comment = body == null ? null : (String) body.get("reviewComment");
        return R.ok().data("contract", contractContractService.approve(id, comment));
    }

    // 5.7 审核驳回（状态守卫，见 Service）
    @PostMapping("/{id}/reject")
    public R reject(@PathVariable("id") Long id, @RequestBody Map<String,Object> body) {
        return R.ok().data("contract", contractContractService.reject(id, body == null ? null : (String) body.get("reviewComment")));
    }

    // 5.8 归档合同（状态守卫 + 事务在 Service 层）
    @PostMapping("/{id}/archive")
    public R archive(@PathVariable("id") Long id, @RequestBody(required = false) Map<String,Object> body) {
        if (body == null) body = Map.of();
        String storageType = (String) body.getOrDefault("storageType", "ELECTRONIC");
        String archiveLocation = (String) body.get("archiveLocation");
        Integer retentionPeriod = body.get("retentionPeriod") == null ? null : Integer.valueOf(String.valueOf(body.get("retentionPeriod")));
        return R.ok().data("archive", contractContractService.archive(id, storageType, archiveLocation, retentionPeriod));
    }

/** 取扩展名（小写），无法解析返回 null */
private String extractExt(String original) {
    if (original == null) return null;
    int idx = original.lastIndexOf('.');
    if (idx <= 0 || idx == original.length() - 1) return null;
    return original.substring(idx + 1).toLowerCase();
    }
}
