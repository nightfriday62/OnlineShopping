package com.nightfriday.onlineshopping.db.dao;

import com.nightfriday.onlineshopping.db.po.OnlineShoppingUser;

public interface OnlineShoppingUserDao {
    int deleteByPrimaryKey(Long userId);

    int insert(OnlineShoppingUser record);

    int insertSelective(OnlineShoppingUser record);

    OnlineShoppingUser selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(OnlineShoppingUser record);

    int updateByPrimaryKey(OnlineShoppingUser record);
}
