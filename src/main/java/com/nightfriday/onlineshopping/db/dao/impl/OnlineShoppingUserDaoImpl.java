package com.nightfriday.onlineshopping.db.dao.impl;

import com.nightfriday.onlineshopping.db.dao.OnlineShoppingUserDao;
import com.nightfriday.onlineshopping.db.mappers.OnlineShoppingUserMapper;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingUser;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class OnlineShoppingUserDaoImpl implements OnlineShoppingUserDao {
 @Resource
 private OnlineShoppingUserMapper onlineShoppingUserMapper;

 @Override
 public int deleteByPrimaryKey(Long userId) {
   return onlineShoppingUserMapper.deleteByPrimaryKey(userId);
 }

 @Override
 public int insert(OnlineShoppingUser record) {
   return onlineShoppingUserMapper.insert(record);
 }

 @Override
 public int insertSelective(OnlineShoppingUser record) {
   return onlineShoppingUserMapper.insertSelective(record);
 }

 @Override
 public OnlineShoppingUser selectByPrimaryKey(Long userId) {
   return onlineShoppingUserMapper.selectByPrimaryKey(userId);
 }

 @Override
 public int updateByPrimaryKeySelective(OnlineShoppingUser record) {
   return onlineShoppingUserMapper.updateByPrimaryKeySelective(record);
 }

 @Override
 public int updateByPrimaryKey(OnlineShoppingUser record) {
   return onlineShoppingUserMapper.updateByPrimaryKey(record);
 }
}
