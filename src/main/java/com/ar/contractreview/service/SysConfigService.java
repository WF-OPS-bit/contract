package com.ar.contractreview.service;

import com.ar.contractreview.entity.SysConfig;
import com.ar.contractreview.security.dto.ConfigSaveRequest;
import com.ar.contractreview.vo.ConfigVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 系统配置表 服务类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
public interface SysConfigService extends IService<SysConfig> {

    /**
     * 15.1 获取配置列表（可按配置类型筛选）
     *
     * @param configType 配置类型（SYSTEM / BUSINESS，可为空）
     * @return 配置列表
     */
    List<ConfigVO> listConfigs(String configType);

    /**
     * 15.2 获取配置详情
     *
     * @param id 配置ID
     * @return 配置详情
     */
    ConfigVO getConfigDetail(Long id);

    /**
     * 15.3 新增配置
     *
     * @param request 配置请求体
     * @return 新增后的配置
     */
    SysConfig addConfig(ConfigSaveRequest request);

    /**
     * 15.4 更新配置
     *
     * @param id      配置ID
     * @param request 配置请求体
     * @return 更新后的配置
     */
    SysConfig updateConfig(Long id, ConfigSaveRequest request);

    /**
     * 15.5 删除配置
     *
     * @param id 配置ID
     */
    void deleteConfig(Long id);
}
