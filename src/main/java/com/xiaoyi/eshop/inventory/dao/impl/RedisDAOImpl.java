package com.xiaoyi.eshop.inventory.dao.impl;

import com.xiaoyi.eshop.inventory.dao.RedisDAO;
import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import redis.clients.jedis.JedisCluster;

@Repository("redisDAO")
public class RedisDAOImpl implements RedisDAO {

    @Resource
    private JedisCluster jedisCluster;

    @Override
    public void set(String key, String value) {
        jedisCluster.set(key, value);
    }

    @Override
    public String get(String key) {
        return jedisCluster.get(key);
    }
}
