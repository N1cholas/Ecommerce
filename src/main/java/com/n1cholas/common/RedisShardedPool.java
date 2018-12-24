package com.n1cholas.common;

import com.n1cholas.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

public class RedisShardedPool {
    //jedis连接池
    private static ShardedJedisPool pool;

    private static String ip1 = PropertiesUtil.getProperty("redis1.ip");
    private static Integer port1 = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));

    private static String ip2 = PropertiesUtil.getProperty("redis2.ip");
    private static Integer port2 = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));

    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10"));
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));
    private static Boolean testOnRetur = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "true"));

    private static void initPool () {
        JedisPoolConfig config = new JedisPoolConfig();

        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnRetur);

        //连接耗尽是否阻塞
        config.setBlockWhenExhausted(true);

        JedisShardInfo info1 = new JedisShardInfo(ip1, port1, 1000*2);
//        info1.setPassword("paper0423");
        JedisShardInfo info2 = new JedisShardInfo(ip2, port2, 1000*2);
//        info2.setPassword("paper0423");

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<JedisShardInfo>();

        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);

        pool = new ShardedJedisPool(config, jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
    }

    static {
        initPool();
    }

    public static ShardedJedis getJedis() {
        return pool.getResource();
    }

    public static void returnResource(ShardedJedis jedis) {
        pool.returnResource(jedis);
    }

    public static void returnBrokenResource(ShardedJedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    public static void main(String[] args) {
        ShardedJedis jedis = pool.getResource();

        for (int i = 0; i < 10; i++) {
            jedis.set("key" + i, "value" + i);
        }

        returnResource(jedis);

        System.out.println("end");
    }
}
