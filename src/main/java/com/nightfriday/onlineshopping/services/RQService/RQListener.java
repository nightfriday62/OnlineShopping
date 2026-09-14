package com.nightfriday.onlineshopping.services.RQService;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(topic = "orderTopic", consumerGroup = "orderConsumerGroup")
public class RQListener implements RocketMQListener<MessageExt>,
    RocketMQPushConsumerLifecycleListener {
/**
*
 * @param message
*/
  @Override
  public void onMessage(MessageExt message) {
    String body = new String(message.getBody());
    log.info("Received message: {}", body);

  }

/**
*
 * @param consumer
*/
  @Override
  public void prepareStart(DefaultMQPushConsumer consumer) {
    consumer.setMaxReconsumeTimes(3);
    consumer.setConsumeTimeout(30000);
    consumer.setConsumeThreadMax(2);
    consumer.setConsumeThreadMin(2);

  }
}
