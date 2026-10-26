package com.nightfriday.onlineshopping.config;

import co.elastic.clients.transport.TransportUtils;
import javax.net.ssl.SSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class EsConfig {
  @Value("${spring.elasticsearch.host}")
  private String esHost;

  @Value("${spring.elasticsearch.port}")
  private int esPort;

  @Value("${spring.elasticsearch.api-key}")
  private String esApiKey;

  @Value("${spring.elasticsearch.api-key-parameter}")
  private String esApiKeyParameter;

  @Bean
  public ElasticsearchClient esClient(ObjectMapper objectMapper) {
    if (esApiKey == null || esApiKey.isBlank()) {
      esApiKey = getApiKeyFromParameterStore(objectMapper);
    }
    return ElasticsearchClient.of(builder -> builder.apiKey(esApiKey).host(esHost + ":" + esPort));
  }

  private String getApiKeyFromParameterStore(ObjectMapper objectMapper) {
    try (SsmClient ssm = SsmClient.create()) {
      String jsonString =
          ssm.getParameter(
                  GetParameterRequest.builder()
                      .name(esApiKeyParameter)
                      .withDecryption(true)
                      .build())
              .parameter()
              .value();
      JsonNode encoded = objectMapper.readTree(jsonString).path("encoded");
      if (!encoded.isString() || encoded.asString().isBlank()) {
        throw new IllegalStateException("SSM Elasticsearch API-key parameter has no encoded value");
      }
        return encoded.asString();
    } catch (Exception e) {
      throw new RuntimeException("Failed to retrieve Elasticsearch API key from SSM Parameter Store", e);
    }

  }
}
