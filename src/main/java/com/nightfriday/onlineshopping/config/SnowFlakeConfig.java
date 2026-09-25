package com.nightfriday.onlineshopping.config;

import com.nightfriday.onlineshopping.services.Snowflake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowFlakeConfig {
  @Value("${snowflake.workerId:1}")private long workerId;
  @Bean
  public Snowflake snowflake() {
    return new Snowflake(workerId);
  }
}
