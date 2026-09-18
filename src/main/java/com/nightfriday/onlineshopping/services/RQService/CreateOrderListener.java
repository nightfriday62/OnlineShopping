package com.nightfriday.onlineshopping.services.RQService;

import com.alibaba.fastjson.JSON;
import com.nightfriday.onlineshopping.db.dao.OnlineShoppingCommodityDao;
import com.nightfriday.onlineshopping.db.dao.OnlineShoppingOrderDao;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingOrder;
import java.util.Date;
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
@RocketMQMessageListener(topic = "createOrder", consumerGroup = "orderGroup")
public class CreateOrderListener
    implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

  @Autowired private OnlineShoppingOrderDao onlineShoppingOrderDao;
  @Autowired private OnlineShoppingCommodityDao onlineShoppingCommodityDao;
  @Autowired private RocketMQService rocketMQService;

  /**
   * @param message
   */
  @Override
  public void onMessage(MessageExt message) {
    String body = new String(message.getBody());
    log.info("Received CreateOrder message: {}", body);
    //    long nanos = 1000000000L;
    //    LockSupport.parkNanos(nanos);
    OnlineShoppingOrder order = JSON.parseObject(body, OnlineShoppingOrder.class);
    Long orderAmount = order.getOrderAmount();
    int stockDecreaseAmount = orderAmount == null ? 1 : Math.toIntExact(orderAmount);
    boolean decreased =
        onlineShoppingCommodityDao.decreaseAvailableStock(
            order.getCommodityId(), stockDecreaseAmount);
    if (decreased) {
      order.setCreateTime(new Date());
      onlineShoppingOrderDao.insert(order);
      rocketMQService.sendDelayMessage("PaymentCheck", JSON.toJSONString(order),4);
      log.info("Added order to DB:" + order);
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
