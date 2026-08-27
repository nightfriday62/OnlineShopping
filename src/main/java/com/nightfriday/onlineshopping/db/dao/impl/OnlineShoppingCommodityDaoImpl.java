package com.nightfriday.onlineshopping.db.dao.impl;

import com.nightfriday.onlineshopping.db.dao.OnlineShoppingCommodityDao;
import com.nightfriday.onlineshopping.db.mappers.OnlineShoppingCommodityMapper;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingCommodity;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class OnlineShoppingCommodityDaoImpl implements OnlineShoppingCommodityDao {
  @Resource
  private OnlineShoppingCommodityMapper onlineShoppingCommodityMapper;
  @Override
  public int deleteByPrimaryKey(Long commodityId) {
    return onlineShoppingCommodityMapper.deleteByPrimaryKey(commodityId);
  }

  @Override
  public int insert(OnlineShoppingCommodity record) {
    return onlineShoppingCommodityMapper.insert(record);
  }

  @Override
  public int insertSelective(OnlineShoppingCommodity record) {
    return onlineShoppingCommodityMapper.insertSelective(record);
  }

  @Override
  public int updateByPrimaryKeySelective(OnlineShoppingCommodity record) {
    return onlineShoppingCommodityMapper.updateByPrimaryKeySelective(record);
  }

  @Override
  public int updateByPrimaryKey(OnlineShoppingCommodity record) {
    return onlineShoppingCommodityMapper.updateByPrimaryKey(record);
  }

  @Override
  public OnlineShoppingCommodity selectByPrimaryKey(Long commodityId) {
    return onlineShoppingCommodityMapper.selectByPrimaryKey(commodityId);
  }

  @Override
  public List<OnlineShoppingCommodity> selectByUserId(Long userId) {
    return onlineShoppingCommodityMapper.selectByUserId(userId);
  }
}
