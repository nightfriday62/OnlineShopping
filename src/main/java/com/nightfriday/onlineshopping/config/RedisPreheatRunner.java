package com.nightfriday.onlineshopping.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.nightfriday.onlineshopping.db.dao.OnlineShoppingCommodityDao;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingCommodity;
import com.nightfriday.onlineshopping.services.EsService;
import com.nightfriday.onlineshopping.services.RedisService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisPreheatRunner implements ApplicationRunner {
  @Autowired private RedisService redisService;
  @Autowired private OnlineShoppingCommodityDao commodityDao;
  @Autowired private EsService esService;

  /**
   * @param args incoming application arguments
   * @throws Exception
   */
  @Override
  public void run(ApplicationArguments args) throws Exception {
    log.info("Preheating Redis with commodity stock data...");
    List<OnlineShoppingCommodity> commodities = commodityDao.selectAll();
    for (OnlineShoppingCommodity commodity : commodities) {
      log.info("Preheating commodity: {}", commodity);
      String key = "commodity_stock_" + commodity.getCommodityId();
      String value = String.valueOf(commodity.getAvailableStock());
      redisService.setValue(key, value);
      log.info("Set Redis key: {}, value: {}", key, value);
      esService.addCommodityToEs(commodity);
    }
    log.info("Redis preheating completed.");
  }
}
