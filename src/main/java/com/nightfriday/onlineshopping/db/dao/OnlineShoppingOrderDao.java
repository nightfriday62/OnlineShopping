package com.nightfriday.onlineshopping.db.dao;

import com.nightfriday.onlineshopping.db.po.OnlineShoppingOrder;

public interface OnlineShoppingOrderDao {
  int insert(OnlineShoppingOrder record);
  int insertSelective(OnlineShoppingOrder record);
  OnlineShoppingOrder selectByPrimaryKey(Long id, Long userId);
  int updateByPrimaryKeySelective(OnlineShoppingOrder record);
  int updateByPrimaryKey(OnlineShoppingOrder record);
  int deleteByPrimaryKey(Long id, Long userId);

  OnlineShoppingOrder selectByOrderNo(String orderNum);
  int setOrderStatus(Long orderId, Long userId, int status);
}
