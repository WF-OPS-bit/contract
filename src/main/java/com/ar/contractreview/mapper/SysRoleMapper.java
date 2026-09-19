package com.ar.contractreview.mapper;
import com.ar.contractreview.entity.SysRole;
import com.ar.contractreview.vo.RoleVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
/**
 * <p>
 * 系统角色表 Mapper 接口
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {
    // 分页查询角色列表（包含用户数统计）
    List<RoleVO> selectRolePage(@Param("offset") int offset, @Param("size") int size);

    // 查询角色总数
    Long countRolePage();

    // 根据ID查询角色
    SysRole selectById(@Param("id") Long id);

    // 根据角色编码查询角色（用于唯一性校验）
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);

    // 插入角色，自增主键通过 useGeneratedKeys 回填到 role.id
    int insertRole(SysRole role);

    // 更新角色
    int updateRole(SysRole role);

    // 删除角色（逻辑删除）
    int deleteRole(@Param("id") Long id, @Param("updatedBy") String updatedBy);
}
