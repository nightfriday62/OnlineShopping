package com.nightfriday.onlineshopping.db.dao;

import com.nightfriday.onlineshopping.db.po.OnlineShoppingCommodity;
import java.util.List;

public interface OnlineShoppingCommodityDao {
  int deleteByPrimaryKey(Long commodityId);

  int insert(OnlineShoppingCommodity record);

  int insertSelective(OnlineShoppingCommodity record);

  int updateByPrimaryKeySelective(OnlineShoppingCommodity record);

  int updateByPrimaryKey(OnlineShoppingCommodity record);

  OnlineShoppingCommodity selectByPrimaryKey(Long commodityId);
  List<OnlineShoppingCommodity> selectByUserId(Long userId);
}
