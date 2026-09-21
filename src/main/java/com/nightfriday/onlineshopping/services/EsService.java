package com.nightfriday.onlineshopping.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.CreateResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingCommodity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EsService {
  @Autowired
  ElasticsearchClient esClient;
  private final String INDEX_NAME = "commodities";

  public void addCommodityToEs(OnlineShoppingCommodity commodity) {
    try {
      
      boolean indexExists = esClient.indices().exists(e -> e.index(INDEX_NAME)).value();
        if (!indexExists) {
          esClient.indices().create(c -> c
              .index("commodities")
              .settings(s -> s
                  .analysis(a -> a
                      .analyzer("ChineseAnalyzer", an -> an
                          .custom(cu -> cu.tokenizer("ik_smart"))
                      )
                      )
              )
              .mappings(m -> m
                  .properties("commodityName", p -> p.text(t -> t.analyzer("ChineseAnalyzer")))
                  .properties("commodityDesc", p -> p.text(t -> t.analyzer("ChineseAnalyzer")))
                  .properties("commodityId", p -> p.keyword(k-> k))
              )
          );
        }

      IndexResponse indexRespond = esClient.index(i -> i
          .index(INDEX_NAME)
          .document(commodity));
      log.info(indexRespond.result().toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public List<OnlineShoppingCommodity> searchCommodity (String keyword) {
    List<OnlineShoppingCommodity> result = new ArrayList<>();
    try {
      SearchResponse<OnlineShoppingCommodity> searchResponse = esClient.search(s -> s
          .index(INDEX_NAME)
          .from(0)
          .size(100)
          .query(q -> q
              .multiMatch(mm -> mm
                  .query(keyword)
                  .fields("commodityName", "commodityDesc"))), OnlineShoppingCommodity.class);

      log.info(searchResponse.toString());
      searchResponse.hits().hits().forEach(hit -> result.add(hit.source()));
      return result;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
