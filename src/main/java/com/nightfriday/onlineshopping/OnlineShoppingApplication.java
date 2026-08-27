package com.nightfriday.onlineshopping;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.nightfriday.onlineshopping.db.mappers")
public class OnlineShoppingApplication {

  public static void main(String[] args) {
    SpringApplication.run(OnlineShoppingApplication.class, args);
  }

}
