package com.ar.contractreview.controller;

import com.ar.contractreview.entity.SysConfig;
import com.ar.contractreview.security.dto.ConfigSaveRequest;
import com.ar.contractreview.result.R;
import com.ar.contractreview.service.SysConfigService;
import com.ar.contractreview.vo.ConfigVO;
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

import java.util.List;

/**
 * <p>
 * 系统配置表 前端控制器
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/configs")
public class SysConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    /**
     * 15.1 获取配置列表（可按配置类型筛选）
     * GET /configs
     */
    @GetMapping
    public R list(@RequestParam(value = "configType", required = false) String configType) {
        List<ConfigVO> list = sysConfigService.listConfigs(configType);
        return R.ok().data(list);
    }

    /**
     * 15.2 获取配置详情
     * GET /configs/{id}
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable Long id) {
        ConfigVO vo = sysConfigService.getConfigDetail(id);
        return R.ok()
                .data("id", vo.getId())
                .data("configKey", vo.getConfigKey())
                .data("configValue", vo.getConfigValue())
                .data("configName", vo.getConfigName())
                .data("configType", vo.getConfigType())
                .data("description", vo.getDescription())
                .data("status", vo.getStatus());
    }

    /**
     * 15.3 新增配置
     * POST /configs
     */
    @PostMapping
    public R add(@RequestBody ConfigSaveRequest request) {
        SysConfig config = sysConfigService.addConfig(request);
        return R.ok()
                .data("id", config.getId())
                .data("configKey", config.getConfigKey())
                .data("configValue", config.getConfigValue())
                .data("configName", config.getConfigName());
    }

    /**
     * 15.4 更新配置
     * PUT /configs/{id}
     */
    @PutMapping("/{id}")
    public R update(@PathVariable Long id, @RequestBody ConfigSaveRequest request) {
        SysConfig config = sysConfigService.updateConfig(id, request);
        return R.ok()
                .data("id", config.getId())
                .data("configKey", config.getConfigKey())
                .data("configValue", config.getConfigValue())
                .data("status", config.getStatus());
    }

    /**
     * 15.5 删除配置
     * DELETE /configs/{id}
     */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        sysConfigService.deleteConfig(id);
        return R.ok().data(true);
    }
}
