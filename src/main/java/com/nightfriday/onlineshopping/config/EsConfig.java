package com.nightfriday.onlineshopping.config;

import co.elastic.clients.transport.TransportUtils;
import javax.net.ssl.SSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import co.elastic.clients.elasticsearch.ElasticsearchClient;

@Configuration
public class EsConfig {
  @Value("${spring.elasticsearch.host}")
  private String esHost;
  @Value("${spring.elasticsearch.port}")
  private int esPort;
  @Value("${spring.elasticsearch.api-key}")
  private String esApiKey;

  @Bean
  public ElasticsearchClient esClient() {
    return ElasticsearchClient.of(builder -> builder
        .apiKey(esApiKey)
        .host(esHost + ":" + esPort));
  }

}
