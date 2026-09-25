package com.nightfriday.onlineshopping.services;

import com.alibaba.fastjson.JSON;
import com.nightfriday.onlineshopping.db.dao.OnlineShoppingCommodityDao;
import com.nightfriday.onlineshopping.db.dao.OnlineShoppingOrderDao;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingCommodity;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingOrder;
import com.nightfriday.onlineshopping.services.RQService.RocketMQService;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderService {
  @Autowired private OnlineShoppingOrderDao orderDao;
  @Autowired private OnlineShoppingCommodityDao commodityDao;
  @Autowired private RedisService redisService;
  @Autowired private RocketMQService rocketMQService;
  @Autowired private Snowflake snowflake;

  public OnlineShoppingOrder createOnlineShoppingOrderFinal(Long userId, Long commodityId) {
//    if (redisService.isInDenyList(String.valueOf(userId), String.valueOf(commodityId))) {
//      log.info("User {} is in deny list for commodity {}", userId, commodityId);
//      return null;
//    }
    String redisKey = "commodity_stock_" + commodityId;
    Long remainStock = redisService.stockDeduct(redisKey);
    if (remainStock >= 0) {
      OnlineShoppingOrder order = createOrder(commodityId, userId, 1, false);
      rocketMQService.sendFIFOMessage("createOrder", JSON.toJSONString(order));
      log.info("Order Placed");
      redisService.addToDenyList(String.valueOf(userId), String.valueOf(commodityId));
      return order;
    }
    log.warn("Order Not Placed");
    return null;
  }

  public OnlineShoppingOrder processOrder(Long commodityId, long userId) {
    OnlineShoppingCommodity commodity = commodityDao.selectByPrimaryKey(commodityId);
    if (commodity == null) {
      log.error("Commodity not found for id: {}", commodityId);
      return null;
    }
    int availableStock = commodity.getAvailableStock();
    if (availableStock <= 0) {
      log.info("Process order failed due to no available stock, commodityId:" + commodityId);
      return null;
    }

    // Decrease available stock and increase lock stock
    commodity.setAvailableStock(availableStock - 1);
    commodityDao.updateByPrimaryKeySelective(commodity);

    // Create order

    return createOrder(commodityId, userId, commodity.getPrice(), true);
  }

  public OnlineShoppingOrder processOrderWithRedisLock(Long commodityId, long userId) {
    String requestId = UUID.randomUUID().toString();
    boolean lockAcquired =
        redisService.getDistributedLock("commodity_lock_" + commodityId, requestId, 5000);
    if (!lockAcquired) {
      log.info("Could not acquire lock for commodityId: " + commodityId);
      return null;
    } else {
      try {
        boolean result = commodityDao.decreaseAvailableStock(commodityId, 1);
        if (!result) {
          log.info("Process order failed due to no available stock, commodityId:" + commodityId);
          return null;
        }
      } finally {
        redisService.releaseDistributedLock("commodity_lock_" + commodityId, requestId);
      }
    }
    return createOrder(
        commodityId, userId, commodityDao.selectByPrimaryKey(commodityId).getPrice(), true);
  }

  public OnlineShoppingOrder processOrderWithRedis(Long commodityId, long userId) {
    String stockKey = "commodity_stock_" + commodityId;
    Long stock = redisService.stockDeduct(stockKey);
    if (stock == null || stock < 0) {
      log.info(
          "Process order failed due to no available stock in Redis, commodityId:" + commodityId);
      return null;
    }

    // Create order
    OnlineShoppingCommodity commodity = commodityDao.selectByPrimaryKey(commodityId);
    if (commodity == null) {
      log.error("Commodity not found for id: {}", commodityId);
      return null;
    }

    return createOrder(commodityId, userId, commodity.getPrice(), true);
  }

  public OnlineShoppingOrder createOrder(
      Long commodityId, Long userId, Integer amount, boolean shouldSaveToDB) {
    OnlineShoppingOrder order =
        OnlineShoppingOrder.builder()
            .orderId(snowflake.nextId())
            .commodityId(commodityId)
            .userId(userId)
            .orderAmount(Long.valueOf(amount))
            .orderNo(UUID.randomUUID().toString())
            .createTime(new Date())
            .orderStatus(1)
            .build();
    if (shouldSaveToDB) {
      orderDao.insert(order);
    }
    return order;
  }

  public OnlineShoppingOrder getByOrderNum(String orderNum) {
    return orderDao.selectByOrderNo(orderNum);
  }

  // order status: 0: No stock available, invalid order 1: Order got created, pending for payment 2:
  // finish payment
  public void payOrderProcess(String orderNumber) {
    log.info("process pay order, orderNumber: {}", orderNumber);
    OnlineShoppingOrder order = orderDao.selectByOrderNo(orderNumber);
    if (order == null) {
      log.error("Order not found for orderNumber: {}", orderNumber);
      return;
    } else if (order.getOrderStatus() != 1) {
      log.error("Order status is not pending for payment, orderNumber: {}", orderNumber);
      return;
    }
    order.setOrderStatus(2);
    order.setPayTime(new Date());
    orderDao.updateByPrimaryKeySelective(order);
  }
}
