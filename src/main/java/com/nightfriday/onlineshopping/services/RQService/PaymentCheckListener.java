package com.nightfriday.onlineshopping.services.RQService;

import com.alibaba.fastjson.JSON;
import com.nightfriday.onlineshopping.db.dao.OnlineShoppingCommodityDao;
import com.nightfriday.onlineshopping.db.dao.OnlineShoppingOrderDao;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingOrder;
import com.nightfriday.onlineshopping.services.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(topic = "PaymentCheck", consumerGroup = "PaymentCheckGroup")
public class PaymentCheckListener
    implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

  @Autowired private OnlineShoppingOrderDao onlineShoppingOrderDao;
  @Autowired private OnlineShoppingCommodityDao onlineShoppingCommodityDao;
  @Autowired private RedisService redisService;

  /**
   * @param message
   */
  @Override
  public void onMessage(MessageExt message) {
    String body = new String(message.getBody());
    log.info("Received PaymentCheck message: {}", body);
    //    long nanos = 1000000000L;
    //    LockSupport.parkNanos(nanos);
    OnlineShoppingOrder order = JSON.parseObject(body, OnlineShoppingOrder.class);
    OnlineShoppingOrder orderFromDB = onlineShoppingOrderDao.selectByOrderNo(order.getOrderNo());
    if (orderFromDB != null && orderFromDB.getOrderStatus() != 2) {
      // revert stock in DB
      onlineShoppingCommodityDao.increaseAvailableStock(
          orderFromDB.getCommodityId(), orderFromDB.getOrderAmount().intValue());

      onlineShoppingOrderDao.setOrderStatus(orderFromDB.getOrderId(), orderFromDB.getUserId(), 99);
      // revert stock in Redis
      Long increased =
          redisService.increaseAvailableStock(
              orderFromDB.getCommodityId(), orderFromDB.getOrderAmount().intValue());
      log.info(
          "Increased available stock in Redis for commodityId {}: {}",
          orderFromDB.getCommodityId(),
          increased);
      // remove user from deny list
      redisService.removeFromDenyList(
          String.valueOf(orderFromDB.getUserId()), String.valueOf(orderFromDB.getCommodityId()));

    } else {
      log.info("Order {} has been paid.", order.getOrderNo());
    }
  }

  /**
   * @param consumer
   */
  @Override
  public void prepareStart(DefaultMQPushConsumer consumer) {
    consumer.setMaxReconsumeTimes(3);
    consumer.setConsumeTimeout(30000);
    consumer.setConsumeThreadMax(2);
    consumer.setConsumeThreadMin(1);
  }
}
