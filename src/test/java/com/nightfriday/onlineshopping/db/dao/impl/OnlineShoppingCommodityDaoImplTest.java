package com.nightfriday.onlineshopping.db.dao.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.nightfriday.onlineshopping.db.dao.OnlineShoppingCommodityDao;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingCommodity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OnlineShoppingCommodityDaoImplTest {
  @Autowired private OnlineShoppingCommodityDao onlineShoppingCommodityDaoImpl;

  @Test
  void insert() {
    OnlineShoppingCommodity commodity =
        OnlineShoppingCommodity.builder()
            .commodityName("Test Commodity")
            .commodityDesc("This is a test commodity")
            .price(100)
            .availableStock(10)
            .creatorUserId(1L)
            .totalStock(10)
            .lockStock(0)
            .build();
    int result = onlineShoppingCommodityDaoImpl.insert(commodity);
    assertEquals(1, result);
  }

  @Test
  void selectByPrimaryKey() {
    OnlineShoppingCommodity onlineShoppingCommodity =
        onlineShoppingCommodityDaoImpl.selectByPrimaryKey(1002L);
    assertNotNull(onlineShoppingCommodity);
  }
}
