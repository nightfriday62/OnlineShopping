package com.nightfriday.onlineshopping.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;

@Slf4j
@Service
public class RedisService {
  @Autowired private JedisPool jedisPool;

  public RedisService setValue(String key, String value) {
    try (var jedis = jedisPool.getResource()) {
      jedis.set(key, value);
    } catch (Exception e) {
      log.error("Error setting value in Redis for key {}: {}", key, e.getMessage());
    }
    return this;
  }

  public String getValue(String key) {
    try (var jedis = jedisPool.getResource()) {
      return jedis.get(key);
    } catch (Exception e) {
      log.error("Error getting value from Redis for key {}: {}", key, e.getMessage());
      return null;
    }
  }

  public Long stockDeduct(String key) {
    try (var jedis = jedisPool.getResource()) {
      String script =
          "if redis.call('exists', KEYS[1]) == 1 then\n"
              + " local stock = tonumber(redis.call('get', KEYS[1]))\n"
              + " if (stock<=0) then\n"
              + " return -1\n"
              + " end\n"
              + "\n"
              + " redis.call('decr', KEYS[1]);\n"
              + " return stock - 1;\n"
              + "end\n"
              + "\n"
              + "return -1;";
      Long stock = (Long) jedis.eval(script, 1, key);
      if (stock == null) {
        log.info("There is no stock available");
        return -1L;
      } else {
        log.info("Stock deducted successfully, remaining stock: {}", stock);
        return stock;
      }
    } catch (Exception e) {
      log.error("Error deducting stock in Redis for key {}: {}", key, e.getMessage());
      return -1L;
    }
  }
}
