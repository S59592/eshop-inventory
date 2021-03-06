package com.xiaoyi.eshop.inventory.request;

/**
 * 请求接口
 */
public interface Request {

    void process();

    Integer getProductId();

    default boolean isForceRefresh() {
        return false;
    };
}
