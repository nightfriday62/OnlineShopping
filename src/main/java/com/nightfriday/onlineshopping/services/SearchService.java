package com.nightfriday.onlineshopping.services;

import com.nightfriday.onlineshopping.db.dao.OnlineShoppingCommodityDao;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingCommodity;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SearchService {
  @Autowired
  private OnlineShoppingCommodityDao commodityDao;

  public List<OnlineShoppingCommodity> searchCommodityByDescription(String description) {

    return commodityDao.searchByDescription(description);
  }
}
