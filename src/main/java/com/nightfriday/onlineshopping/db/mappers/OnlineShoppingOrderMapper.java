package com.nightfriday.onlineshopping.db.mappers;

import com.nightfriday.onlineshopping.db.po.OnlineShoppingOrder;

public interface OnlineShoppingOrderMapper {
    int deleteByPrimaryKey(Long orderId);

    int insert(OnlineShoppingOrder record);

    int insertSelective(OnlineShoppingOrder record);

    OnlineShoppingOrder selectByPrimaryKey(Long orderId);

    int updateByPrimaryKeySelective(OnlineShoppingOrder record);

    int updateByPrimaryKey(OnlineShoppingOrder record);

  OnlineShoppingOrder selectByOrderNum(String orderNum);
}