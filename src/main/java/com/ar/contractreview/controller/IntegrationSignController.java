package com.ar.contractreview.controller;

import com.ar.contractreview.entity.IntegrationSign;
import com.ar.contractreview.entity.IntegrationSignRecord;
import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.service.IntegrationSignRecordService;
import com.ar.contractreview.service.IntegrationSignService;
import com.ar.contractreview.utils.SecurityUtils;
import com.ar.contractreview.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 电子签章配置表 前端控制器
 * </p>
 * 说明:响应字段严格按接口文档第 19 章输出(只保留文档列出的字段)。
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/integration/sign")
public class IntegrationSignController {

    @Autowired
    private IntegrationSignService signService;

    @Autowired
    private IntegrationSignRecordService signRecordService;

    /**
     * 19.1 获取签章配置列表
     * 地址:GET /api/integration/sign
     * 文档响应字段:id / signType / signName / status / certificateExpireDate(全量数组,无分页)
     */
    @GetMapping
    public R list() {
        List<Map<String, Object>> list = signService.list().stream()
                .map(this::toSignListVO)
                .collect(Collectors.toList());
        return R.ok().data("list", list);
    }

    /**
     * 19.2 获取签章配置详情
     * 地址:GET /api/integration/sign/{id}
     * 文档响应字段:id / signType / signName / status / appId / certificatePath /
     *              certificateNumber / certificateExpireDate / description
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable(value = "id") Long id) {
        IntegrationSign sign = signService.getById(id);
        if (sign == null) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "签章配置不存在");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", sign.getId());
        data.put("signType", sign.getSignType());
        data.put("signName", sign.getSignName());
        data.put("status", sign.getStatus());
        data.put("appId", sign.getAppId());
        data.put("certificatePath", sign.getCertificatePath());
        data.put("certificateNumber", sign.getCertificateNumber());
        data.put("certificateExpireDate", sign.getCertificateExpireDate());
        data.put("description", sign.getDescription());
        return R.ok().data("data", data);
    }

    /**
     * 19.3 新增签章配置
     * 地址:POST /api/integration/sign,Content-Type: application/json
     * 请求体(文档):signType / signName / appId / appKey / certificatePath
     * 响应(文档):id / signType / signName / status
     */
    @PostMapping
    public R add(@RequestBody IntegrationSign sign) {
        // 必填校验(接口文档:signType、signName 必填)
        if (StringUtils.isEmpty(sign.getSignType())) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "签章类型不能为空");
        }
        if (StringUtils.isEmpty(sign.getSignName())) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "签章名称不能为空");
        }
        // 状态默认 ACTIVE(启用)
        sign.setStatus(StringUtils.isEmpty(sign.getStatus()) ? "ACTIVE" : sign.getStatus());
        signService.save(sign);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", sign.getId());
        data.put("signType", sign.getSignType());
        data.put("signName", sign.getSignName());
        data.put("status", sign.getStatus());
        return R.ok().data("data", data);
    }

    /**
     * 19.4 更新签章配置
     * 地址:PUT /api/integration/sign/{id},Content-Type: application/json
     * 请求体同新增;只更新传了的字段,没传的保持原值。
     * 响应(文档):id / signName / status
     */
    @PutMapping("/{id}")
    public R update(@PathVariable(value = "id") Long id, @RequestBody IntegrationSign req) {
        // 记录必须存在
        IntegrationSign sign = signService.getById(id);
        if (sign == null) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "签章配置不存在");
        }
        // 只应用请求体里传了的字段
        if (!StringUtils.isEmpty(req.getSignType())) {
            sign.setSignType(req.getSignType());
        }
        if (!StringUtils.isEmpty(req.getSignName())) {
            sign.setSignName(req.getSignName());
        }
        if (!StringUtils.isEmpty(req.getAppId())) {
            sign.setAppId(req.getAppId());
        }
        if (!StringUtils.isEmpty(req.getAppKey())) {
            sign.setAppKey(req.getAppKey());
        }
        if (!StringUtils.isEmpty(req.getCertificatePath())) {
            sign.setCertificatePath(req.getCertificatePath());
        }
        signService.updateById(sign);

        IntegrationSign latest = signService.getById(id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", latest.getId());
        data.put("signName", latest.getSignName());
        data.put("status", latest.getStatus());
        return R.ok().data("data", data);
    }

    /**
     * 19.5 删除签章配置
     * 地址:DELETE /api/integration/sign/{id}
     * 说明:integration_sign 表没有 deleted 字段,removeById 为物理删除。
     */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable(value = "id") Long id) {
        boolean ok = signService.removeById(id);
        return R.ok().data("data", ok);
    }

    /**
     * 19.6 获取签章记录列表(分页)
     * 地址:GET /api/integration/sign/records?page=1&size=10&contractId=&verifyStatus=
     * 文档响应字段:id / contractId / contractNo / signTime / signUserName / signatureImage / verifyStatus + total
     * 说明:路径 /records 是固定段,优先于 /{id} 匹配,不会冲突。
     */
    @GetMapping("/records")
    public R records(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "contractId", required = false) Long contractId,
            @RequestParam(value = "verifyStatus", required = false) String verifyStatus
    ) {
        QueryWrapper<IntegrationSignRecord> wrapper = new QueryWrapper<>();
        if (contractId != null) {
            wrapper.eq("contract_id", contractId);
        }
        if (!StringUtils.isEmpty(verifyStatus)) {
            wrapper.eq("verify_status", verifyStatus);
        }
        // 最新签章在前
        wrapper.orderByDesc("sign_time");

        IPage<IntegrationSignRecord> pageResult = signRecordService.page(new Page<>(page, size), wrapper);
        List<Map<String, Object>> list = pageResult.getRecords().stream()
                .map(this::toRecordVO)
                .collect(Collectors.toList());
        return R.ok()
                .data("list", list)
                .data("total", pageResult.getTotal());
    }

    /**
     * 19.7 签署合同
     * 地址:POST /api/integration/sign/{contractId}
     * 请求体(文档):signId / signPosition
     * 响应(文档):recordId / contractId / signTime / verifyStatus / message
     * 说明:为指定合同创建一条签章记录(integration_sign_record),
     *       签章人取当前登录用户;初始验证状态 PENDING(待验证)。
     */
    @PostMapping("/{contractId}")
    public R signContract(@PathVariable(value = "contractId") Long contractId,
                          @RequestBody IntegrationSignRecord req) {
        // 签章配置 ID 必填(用哪一套签章)
        if (req.getSignId() == null) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "签章配置ID不能为空");
        }

        IntegrationSignRecord record = new IntegrationSignRecord()
                .setSignId(req.getSignId())
                .setContractId(contractId)
                .setSignPosition(req.getSignPosition())
                .setSignTime(LocalDateTime.now())
                .setCreatedTime(LocalDateTime.now())
                .setSignUserId(SecurityUtils.currentUserId())
                .setSignUserName(SecurityUtils.currentUsername())
                .setVerifyStatus("PENDING");
        signRecordService.save(record);

        // 按接口文档组装响应
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("recordId", record.getId());
        data.put("contractId", contractId);
        data.put("signTime", record.getSignTime());
        data.put("verifyStatus", "PENDING");
        data.put("message", "签章已提交，等待验证");
        return R.ok().data("data", data);
    }

    /**
     * 签章配置列表项 —— 只保留接口文档 19.1 列出的字段
     */
    private Map<String, Object> toSignListVO(IntegrationSign sign) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", sign.getId());
        item.put("signType", sign.getSignType());
        item.put("signName", sign.getSignName());
        item.put("status", sign.getStatus());
        item.put("certificateExpireDate", sign.getCertificateExpireDate());
        return item;
    }

    /**
     * 签章记录列表项 —— 只保留接口文档 19.6 列出的字段
     */
    private Map<String, Object> toRecordVO(IntegrationSignRecord record) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", record.getId());
        item.put("contractId", record.getContractId());
        item.put("contractNo", record.getContractNo());
        item.put("signTime", record.getSignTime());
        item.put("signUserName", record.getSignUserName());
        item.put("signatureImage", record.getSignatureImage());
        item.put("verifyStatus", record.getVerifyStatus());
        return item;
    }
}
