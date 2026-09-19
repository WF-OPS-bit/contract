package com.ar.contractreview.security.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 角色管理 - 分配角色菜单权限请求对象
 * </p>
 * <p>
 * 对应接口：POST /role/{id}/menus
 * </p>
 *
 * @author wyh
 * @since 2026-09-07
 */
@Data
public class RoleMenuRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 菜单ID列表（必填）
     */
    private List<Long> menuIds;
}
