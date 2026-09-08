package com.nightfriday.onlineshopping.db.dao;

import com.nightfriday.onlineshopping.db.po.OnlineShoppingOrder;

public interface OnlineShoppingOrderDao {
  int insert(OnlineShoppingOrder record);
  int insertSelective(OnlineShoppingOrder record);
  OnlineShoppingOrder selectByPrimaryKey(Long id);
  int updateByPrimaryKeySelective(OnlineShoppingOrder record);
  int updateByPrimaryKey(OnlineShoppingOrder record);
  int deleteByPrimaryKey(Long id);

  OnlineShoppingOrder selectByOrderNo(String orderNum);
}
