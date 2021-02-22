package com.xiaoyi.eshop.inventory.service;

import com.xiaoyi.eshop.inventory.request.Request;

/**
 * 请求异步执行的service
 * @author Dong Jie
 */
public interface RequestAsyncProcessService {

    void process(Request request);
}
