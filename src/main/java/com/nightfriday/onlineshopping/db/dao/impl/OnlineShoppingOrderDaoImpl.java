package com.nightfriday.onlineshopping.db.dao.impl;

import com.nightfriday.onlineshopping.db.dao.OnlineShoppingOrderDao;
import com.nightfriday.onlineshopping.db.mappers.OnlineShoppingOrderMapper;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingOrder;
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
  public OnlineShoppingOrder selectByPrimaryKey(Long id) {
    return mapper.selectByPrimaryKey(id);
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
  public int deleteByPrimaryKey(Long id) {
    return mapper.deleteByPrimaryKey(id);
  }
}
