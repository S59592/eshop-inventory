package com.xiaoyi.eshop.inventory.service;

import com.xiaoyi.eshop.inventory.model.User;

/**
 * 用户Service接口
 * @author Dong jie
 *
 */
public interface UserService {

    /**
     * 查询用户信息
     * @return 用户信息
     */
    User findUserInfo();

    /**
     * 查询redis中缓存的用户信息
     * @return
     */
    User getCachedUserInfo();
}
