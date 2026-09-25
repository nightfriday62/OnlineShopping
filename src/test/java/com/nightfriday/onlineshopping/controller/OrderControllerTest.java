package com.nightfriday.onlineshopping.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderControllerTest {
  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void buyCommodity() {
    for (long i = 1; i <= 3; i++) {
      for (long j = 1006; j <= 1009; j++) {
        String s = String.format("http://localhost/commodity/buy/commodityId=%d/userId=%d", i, j);
        restTemplate.getForEntity(s, String.class);
      }
    }
  }
}
