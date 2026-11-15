package com.renewcommunity.renewcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.renewcommunity.renewcommunity.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统角色数据访问。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询用户已启用的角色。
     */
    @Select("""
            SELECT r.*
            FROM sys_role r
            JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId} AND r.status = 1
            ORDER BY r.sort, r.id
            """)
    List<SysRole> selectEnabledByUserId(@Param("userId") Long userId);
}
