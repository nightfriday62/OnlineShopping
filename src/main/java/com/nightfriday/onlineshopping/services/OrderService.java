package com.nightfriday.onlineshopping.services;

import com.nightfriday.onlineshopping.db.dao.OnlineShoppingCommodityDao;
import com.nightfriday.onlineshopping.db.dao.OnlineShoppingOrderDao;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingCommodity;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingOrder;
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

    return createOrder(commodityId, userId, commodity.getPrice());
  }

  public OnlineShoppingOrder createOrder(Long commodityId, Long userId, Integer price) {
    OnlineShoppingOrder order =
        OnlineShoppingOrder.builder()
            .commodityId(commodityId)
            .userId(userId)
            .orderAmount(Long.valueOf(price))
            .orderNo(UUID.randomUUID().toString())
            .createTime(new Date())
            .orderStatus(1)
            .build();
    orderDao.insert(order);
    return order;
  }
  public OnlineShoppingOrder getByOrderNum(String orderNum) {
    return orderDao.selectByOrderNo(orderNum);
  }
  // order status: 0: No stock available, invalid order 1: Order got created, pending for payment 2: finish payment
  public void payOrderProcess(String orderNumber) {
    log.info("process pay order, orderNumber: {}", orderNumber);
    OnlineShoppingOrder order = orderDao.selectByOrderNo(orderNumber);
    if (order == null) {
      log.error("Order not found for orderNumber: {}", orderNumber);
      return;
    }else if (order.getOrderStatus() != 1) {
      log.error("Order status is not pending for payment, orderNumber: {}", orderNumber);
      return;
    }
    order.setOrderStatus(2);
    order.setPayTime(new Date());
    orderDao.updateByPrimaryKeySelective(order);
  }
}
