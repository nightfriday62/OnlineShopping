package com.nightfriday.onlineshopping.services.RQService;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RocketMQServiceTest {
  @Autowired
  private RocketMQService rocketMQService;
  @Test
  void sendFIFOMessage() {
    String topic = "orderTopic";
    String message = "Test FIFO message";
    for (int i = 0; i < 10; i++) {
      rocketMQService.sendFIFOMessage(topic, message + " " + i);
    }
  }
}
