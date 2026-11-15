package com.renewcommunity.renewcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.renewcommunity.renewcommunity.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户数据访问。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
