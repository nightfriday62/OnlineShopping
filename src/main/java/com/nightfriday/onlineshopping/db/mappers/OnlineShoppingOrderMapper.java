package com.nightfriday.onlineshopping.db.mappers;

import com.nightfriday.onlineshopping.db.po.OnlineShoppingOrder;
import org.apache.ibatis.annotations.Param;

public interface OnlineShoppingOrderMapper {
    int deleteByPrimaryKey(@Param("orderId") Long orderId, @Param("userId") Long userId);

    int insert(OnlineShoppingOrder record);

    int insertSelective(OnlineShoppingOrder record);

    OnlineShoppingOrder selectByPrimaryKey(@Param("orderId") Long orderId, @Param("userId") Long userId);

    int updateByPrimaryKeySelective(OnlineShoppingOrder record);

    int updateByPrimaryKey(OnlineShoppingOrder record);

  OnlineShoppingOrder selectByOrderNum(String orderNum);

    int setOrderStatus(@Param("orderId") Long orderId, @Param("userId") Long userId, @Param("status") int status);
}