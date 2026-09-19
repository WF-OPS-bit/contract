package com.ar.contractreview.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 系统配置管理 - 响应对象
 * </p>
 * <p>
 * 对应接口：GET /configs（列表）、GET /configs/{id}（详情）
 * </p>
 *
 * @author wyh
 * @since 2026-09-16
 */
@Data
public class ConfigVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    private Long id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置类型：SYSTEM-系统配置，BUSINESS-业务配置
     */
    private String configType;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    private Byte status;
}
