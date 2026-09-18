package com.nightfriday.onlineshopping.db.mappers;

import com.nightfriday.onlineshopping.db.po.OnlineShoppingCommodity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OnlineShoppingCommodityMapper {
    int deleteByPrimaryKey(Long commodityId);

    int insert(OnlineShoppingCommodity record);

    int insertSelective(OnlineShoppingCommodity record);

    OnlineShoppingCommodity selectByPrimaryKey(Long commodityId);

    int updateByPrimaryKeySelective(OnlineShoppingCommodity record);

    int updateByPrimaryKey(OnlineShoppingCommodity record);

  List<OnlineShoppingCommodity> selectByUserId(Long userId);

  List<OnlineShoppingCommodity> selectAll();

  int decreaseAvailableStock(@Param("commodityId") Long commodityId, @Param("amount") int amount);

  int increaseAvailableStock(Long commodityId, int amount);
}