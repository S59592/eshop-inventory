package com.xiaoyi.eshop.inventory.listener;

import com.xiaoyi.eshop.inventory.thread.RequestProcessorThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class InitListener implements ServletContextListener {

    private final Logger logger = LoggerFactory.getLogger(InitListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        RequestProcessorThreadPool.init();
        logger.info("初始化工作线程池和内存队列");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("系统监听器销毁");
    }
}
