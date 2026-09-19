package com.ar.contractreview.security.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 角色管理 - 新增 / 更新角色请求对象
 * </p>
 * <p>
 * 对应接口：POST /role（新增）、PUT /role/{id}（更新）
 * </p>
 *
 * @author wyh
 * @since 2026-09-07
 */
@Data
public class RoleSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色名称（必填）
     */
    private String roleName;

    /**
     * 角色编码（必填，唯一）
     */
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 权限菜单ID列表
     */
    private List<Long> menuIds;
}
