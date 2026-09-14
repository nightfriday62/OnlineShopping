package com.nightfriday.onlineshopping.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

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
      if (stock == -1L) {
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

  /**
   * @param lockKey the key for the lock in Redis
   * @param requestId the unique identifier for the lock request
   * @param expireTime in milliseconds
   * @return true if the lock was acquired successfully, false otherwise
   */
  public boolean getDistributedLock(String lockKey, String requestId, int expireTime) {
    try (var jedis = jedisPool.getResource()) {
      SetParams params = new SetParams();
      params.nx().px(expireTime);
      String result = jedis.set(lockKey, requestId, params);
      return "OK".equals(result);
    } catch (Exception e) {
      log.error("Error acquiring distributed lock for key {}: {}", lockKey, e.getMessage());
      return false;
    }
  }

  /**
   * @param lockKey the key for the lock in Redis
   * @param requestId the unique identifier for the lock request
   * @return true if the lock was released successfully, false otherwise
   */
  public boolean releaseDistributedLock(String lockKey, String requestId) {
    try (var jedis = jedisPool.getResource()) {
      String script =
          "if redis.call('get', KEYS[1]) == ARGV[1] then\n"
              + "    return redis.call('del', KEYS[1])\n"
              + "else\n"
              + "    return 0\n"
              + "end";
      Object result = jedis.eval(script, 1, lockKey, requestId);
      return Long.valueOf(1).equals(result);
    } catch (Exception e) {
      log.error("Error releasing distributed lock for key {}: {}", lockKey, e.getMessage());
      return false;
    }
  }
}
