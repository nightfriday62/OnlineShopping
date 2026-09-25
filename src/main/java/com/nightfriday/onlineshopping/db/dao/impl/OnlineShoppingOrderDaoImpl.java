package com.nightfriday.onlineshopping.db.dao.impl;

import com.nightfriday.onlineshopping.db.dao.OnlineShoppingOrderDao;
import com.nightfriday.onlineshopping.db.mappers.OnlineShoppingOrderMapper;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingOrder;
import com.nightfriday.onlineshopping.services.Snowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class OnlineShoppingOrderDaoImpl implements OnlineShoppingOrderDao {
  @Autowired
  private OnlineShoppingOrderMapper mapper;

  @Override
  public int insert(OnlineShoppingOrder record) {
    return mapper.insert(record);
  }

  @Override
  public int insertSelective(OnlineShoppingOrder record) {
    return mapper.insertSelective(record);
  }

  @Override
  public OnlineShoppingOrder selectByPrimaryKey(Long id, Long userId) {
    return mapper.selectByPrimaryKey(id, userId);
  }

  @Override
  public int updateByPrimaryKeySelective(OnlineShoppingOrder record) {
    return mapper.updateByPrimaryKeySelective(record);
  }

  @Override
  public int updateByPrimaryKey(OnlineShoppingOrder record) {
    return mapper.updateByPrimaryKey(record);
  }

  @Override
  public int deleteByPrimaryKey(Long id, Long userId) {
    return mapper.deleteByPrimaryKey(id, userId);
  }

  @Override
  public OnlineShoppingOrder selectByOrderNo(String orderNum) {
    return mapper.selectByOrderNum(orderNum);
  }

  @Override
  public int setOrderStatus(Long orderId, Long userId, int status) {
    return mapper.setOrderStatus(orderId, userId, status);
  }
}
