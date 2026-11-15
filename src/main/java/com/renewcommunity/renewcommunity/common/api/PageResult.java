package com.renewcommunity.renewcommunity.common.api;

import java.util.List;

/**
 * 分页响应。
 *
 * @param total 总记录数
 * @param list  当前页数据
 */
public record PageResult<T>(long total, List<T> list) {
}
