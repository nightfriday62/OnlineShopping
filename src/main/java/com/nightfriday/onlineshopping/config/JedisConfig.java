package com.nightfriday.onlineshopping.config;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.SslOptions;

@Slf4j
@Configuration
public class JedisConfig {
  @Value("${spring.redis.host}")
  private String redisHost;

  @Value("${spring.redis.port}")
  private int redisPort;

  @Value("${spring.redis.timeout:2000}")
  private long timeout;

  @Value("${spring.redis.pool.max-active:8}")
  private int maxActive;

  @Value("${spring.redis.pool.max-idle:8}")
  private int maxIdle;

  @Value("${spring.redis.pool.min-idle:0}")
  private int minIdle;

  @Value("${spring.redis.pool.max-wait:2000}")
  private long maxWait;

  @Bean
  public JedisPool redisPool() {
    int timeoutMillis = Math.toIntExact(timeout);
    JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
        .connectionTimeoutMillis(timeoutMillis)
        .socketTimeoutMillis(timeoutMillis)
        .blockingSocketTimeoutMillis(timeoutMillis)
        .sslOptions(SslOptions.builder().build())
        .build();

    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(maxActive);
    poolConfig.setMaxIdle(maxIdle);
    poolConfig.setMinIdle(minIdle);
    poolConfig.setMaxWait(Duration.ofMillis(maxWait));

    log.info(
        "Initializing JedisPool with host: {}, port: {}, timeoutMs: {}, maxActive: {}, maxIdle: {}, minIdle: {}, maxWaitMs: {}",
        redisHost, redisPort, timeoutMillis, maxActive, maxIdle, minIdle, maxWait);

    return new JedisPool(poolConfig, new HostAndPort(redisHost, redisPort), clientConfig);
  }
}
