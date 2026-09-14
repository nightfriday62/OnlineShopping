package com.nightfriday.onlineshopping.services.RQService;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RocketMQService {
  @Autowired private RocketMQTemplate rocketMQTemplate;

  public void sendFIFOMessage(String topic, String message) {
    Message msg = new Message(topic, message.getBytes());
    try {
      rocketMQTemplate.getProducer().send(msg, ((mqs,msg1,arg) -> mqs.get(0)),10000);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    // rocketMQTemplate.syncSendOrderly(topic, message, "orderId");
  }
}
