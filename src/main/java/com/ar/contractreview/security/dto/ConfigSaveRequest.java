package com.ar.contractreview.security.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 系统配置管理 - 新增 / 更新配置请求对象
 * </p>
 * <p>
 * 对应接口：POST /configs（新增）、PUT /configs/{id}（更新）
 * </p>
 * <p>
 * 新增必填：configKey / configValue / configName；更新必填：configValue
 * </p>
 *
 * @author wyh
 * @since 2026-09-16
 */
@Data
public class ConfigSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置键（新增必填）
     */
    private String configKey;

    /**
     * 配置值（必填）
     */
    private String configValue;

    /**
     * 配置名称（新增必填）
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
     * 状态：0-禁用，1-启用（更新时可选）
     */
    private Byte status;
}
