package com.nightfriday.onlineshopping.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Configuration
public class EsConfig {
  private static final int SSM_MAX_ATTEMPTS = 30;
  private static final long SSM_RETRY_DELAY_MS = 10_000;

  @Value("${spring.elasticsearch.host}")
  private String esHost;

  @Value("${spring.elasticsearch.port}")
  private int esPort;

  @Value("${spring.elasticsearch.api-key}")
  private String esApiKey;

  @Value("${spring.elasticsearch.api-key-parameter}")
  private String esApiKeyParameter;

  @Value("${spring.elasticsearch.ca-cert-parameter}")
  private String esCaCertParameter;

  @Value("${spring.elasticsearch.ca-cert-pem:}")
  private String esCaCertPem;

  @Bean
  public ElasticsearchClient esClient(ObjectMapper objectMapper) throws Exception {
    if (esApiKey == null || esApiKey.isBlank()) {
      esApiKey = getApiKeyFromParameterStore(objectMapper);
    }
    String caPem =
        esCaCertPem == null || esCaCertPem.isBlank()
            ? getParameter(esCaCertParameter, false)
            : esCaCertPem;
    SSLContext sslContext = buildSslContext(caPem);
    String host = "https://" + esHost + ":" + esPort;
    return ElasticsearchClient.of(
        builder -> builder.apiKey(esApiKey).host(host).sslContext(sslContext));
  }

  // Trusts only the Elasticsearch CA; hostname verification stays enabled.
  private static SSLContext buildSslContext(String caPem) throws Exception {
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    trustStore.load(null, null);
    var certificates =
        certificateFactory.generateCertificates(
            new ByteArrayInputStream(caPem.getBytes(StandardCharsets.UTF_8)));
    if (certificates.isEmpty()) {
      throw new IllegalStateException("Elasticsearch CA certificate PEM contains no certificates");
    }
    int i = 0;
    for (var certificate : certificates) {
      trustStore.setCertificateEntry("elasticsearch-ca-" + i++, certificate);
    }
    TrustManagerFactory trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(trustStore);
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
    return sslContext;
  }

  private String getApiKeyFromParameterStore(ObjectMapper objectMapper) {
    String jsonString = getParameter(esApiKeyParameter, true);
    JsonNode encoded = objectMapper.readTree(jsonString).path("encoded");
    if (!encoded.isString() || encoded.asString().isBlank()) {
      throw new IllegalStateException("SSM Elasticsearch API-key parameter has no encoded value");
    }
    return encoded.asString();
  }

  // Retries because the Elasticsearch instance publishes these parameters when its bootstrap finishes.
  private String getParameter(String name, boolean decrypt) {
    return retry(
        "SSM parameter " + name,
        () -> {
          try (SsmClient ssm = SsmClient.create()) {
            return ssm.getParameter(
                    GetParameterRequest.builder().name(name).withDecryption(decrypt).build())
                .parameter()
                .value();
          }
        });
  }

  private static <T> T retry(String what, Supplier<T> action) {
    RuntimeException last = null;
    for (int attempt = 1; attempt <= SSM_MAX_ATTEMPTS; attempt++) {
      try {
        return action.get();
      } catch (RuntimeException e) {
        last = e;
        log.warn("Failed to read {} (attempt {}/{}): {}", what, attempt, SSM_MAX_ATTEMPTS, e.getMessage());
        try {
          Thread.sleep(SSM_RETRY_DELAY_MS);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Interrupted while reading " + what, ie);
        }
      }
    }
    throw new RuntimeException("Failed to read " + what, last);
  }
}
