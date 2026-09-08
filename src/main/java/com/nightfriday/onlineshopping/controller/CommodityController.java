package com.nightfriday.onlineshopping.controller;

import com.nightfriday.onlineshopping.db.dao.OnlineShoppingCommodityDao;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingCommodity;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CommodityController {
  @Autowired private OnlineShoppingCommodityDao dao;

  @RequestMapping("/addItem")
  public String addCommodity() {
    return "add_commodity";
  }

  @RequestMapping("/commodities")
  public String addCommodity(
      @RequestParam String commodityName,
      @RequestParam String commodityDesc,
      @RequestParam Integer price,
      @RequestParam Integer availableStock,
      @RequestParam Long creatorUserId,
      Map<String, Object> model) {
    OnlineShoppingCommodity commodity =
        OnlineShoppingCommodity.builder()
            .commodityName(commodityName)
            .commodityDesc(commodityDesc)
            .price(price)
            .availableStock(availableStock)
            .totalStock(availableStock)
            .creatorUserId(creatorUserId)
            .lockStock(0)
            .build();
    int savedId = dao.insert(commodity);
    commodity.setCommodityId((long) savedId);
    model.put("abc", commodity);
    return "add_commodity_success";
  }

  @GetMapping("/listItems/{sellerId}")
  public String listItems(@PathVariable String sellerId, Map<String, Object> model) {
    List<OnlineShoppingCommodity> commodities =
        dao.listCommoditiesByUserId(Long.parseLong(sellerId));
    model.put("itemList", commodities);
    return "list_items";
  }

  @GetMapping("/item/{commodityId}")
  public String itemDetail(@PathVariable String commodityId, Map<String, Object> model) {
    OnlineShoppingCommodity commodity = dao.selectByPrimaryKey(Long.parseLong(commodityId));
    model.put("commodity", commodity);
    return "item_detail";
  }
}
