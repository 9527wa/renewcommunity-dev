package com.renewcommunity.renewcommunity.model.vo;

import java.util.List;

/**
 * 用户列表项。
 */
public record UserVO(
        Long id,
        String username,
        String nickname,
        String mobile,
        String email,
        Integer status,
        List<Long> roleIds
) {
}
