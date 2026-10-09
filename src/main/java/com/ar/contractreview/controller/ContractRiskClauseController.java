package com.ar.contractreview.controller;

import com.ar.contractreview.result.R;
import com.ar.contractreview.service.ContractRiskClauseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * <p>
 * 风险条款表 前端控制器 —— 《接口文档》6.3 / 6.4「风险处理」
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/risks")
public class ContractRiskClauseController {

    @Autowired
    private ContractRiskClauseService riskClauseService;

    /**
     * 6.3 确认风险
     * <p>POST /api/risks/{id}/confirm，请求体 {"comment": "..."}（可选）</p>
     */
    @PostMapping("/{id}/confirm")
    public R confirm(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> body) {
        String comment = body == null ? null : (String) body.get("comment");
        return R.ok().data(riskClauseService.confirm(id, comment));
    }

    /**
     * 6.4 驳回风险
     * <p>POST /api/risks/{id}/reject，请求体 {"comment": "..."}（驳回理由必填）</p>
     */
    @PostMapping("/{id}/reject")
    public R reject(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> body) {
        String comment = body == null ? null : (String) body.get("comment");
        return R.ok().data(riskClauseService.reject(id, comment));
    }
}
