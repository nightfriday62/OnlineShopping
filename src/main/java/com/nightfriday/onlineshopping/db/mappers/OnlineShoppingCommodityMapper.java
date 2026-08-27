package com.nightfriday.onlineshopping.db.mappers;

import com.nightfriday.onlineshopping.db.po.OnlineShoppingCommodity;
import java.util.List;

public interface OnlineShoppingCommodityMapper {
    int deleteByPrimaryKey(Long commodityId);

    int insert(OnlineShoppingCommodity record);

    int insertSelective(OnlineShoppingCommodity record);

    OnlineShoppingCommodity selectByPrimaryKey(Long commodityId);

    int updateByPrimaryKeySelective(OnlineShoppingCommodity record);

    int updateByPrimaryKey(OnlineShoppingCommodity record);

  List<OnlineShoppingCommodity> selectByUserId(Long userId);
}