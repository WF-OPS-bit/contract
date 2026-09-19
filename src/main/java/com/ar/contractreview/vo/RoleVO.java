package com.ar.contractreview.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 角色管理 - 响应对象
 * </p>
 * <p>
 * 列表接口：id / roleName / roleCode / description / status / userCount
 * 详情接口：id / roleName / roleCode / description / status / menuIds
 * </p>
 *
 * @author wyh
 * @since 2026-09-07
 */
@Data
public class RoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    private Byte status;

    /**
     * 用户数量（列表接口使用，LEFT JOIN sys_user 统计）
     */
    private Long userCount;

    /**
     * 权限菜单ID列表（详情接口使用）
     */
    private List<Long> menuIds = new ArrayList<>();
}
