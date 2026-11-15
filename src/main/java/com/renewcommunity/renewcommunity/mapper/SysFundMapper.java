package com.renewcommunity.renewcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.renewcommunity.renewcommunity.entity.SysFund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 资金台账数据访问。
 */
@Mapper
public interface SysFundMapper extends BaseMapper<SysFund> {

    @Select("SELECT COALESCE(SUM(amount), 0) FROM sys_fund WHERE type = #{type}")
    BigDecimal sumByType(@Param("type") Integer type);
}
