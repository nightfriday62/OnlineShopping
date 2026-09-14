package com.nightfriday.onlineshopping.db.dao.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nightfriday.onlineshopping.db.mappers.OnlineShoppingUserMapper;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OnlineShoppingUserDaoImplTest {
  @Mock private OnlineShoppingUserMapper onlineShoppingUserMapper;

  @InjectMocks private OnlineShoppingUserDaoImpl onlineShoppingUserDao;

  @Test
  void deleteByPrimaryKeyDelegatesToMapper() {
    Long userId = 10L;
    when(onlineShoppingUserMapper.deleteByPrimaryKey(userId)).thenReturn(1);

    int result = onlineShoppingUserDao.deleteByPrimaryKey(userId);

    assertEquals(1, result);
    verify(onlineShoppingUserMapper).deleteByPrimaryKey(userId);
  }

  @Test
  void insertDelegatesToMapper() {
    OnlineShoppingUser record = buildUser();
    when(onlineShoppingUserMapper.insert(record)).thenReturn(1);

    int result = onlineShoppingUserDao.insert(record);

    assertEquals(1, result);
    verify(onlineShoppingUserMapper).insert(record);
  }

  @Test
  void insertSelectiveDelegatesToMapper() {
    OnlineShoppingUser record = buildUser();
    when(onlineShoppingUserMapper.insertSelective(record)).thenReturn(1);

    int result = onlineShoppingUserDao.insertSelective(record);

    assertEquals(1, result);
    verify(onlineShoppingUserMapper).insertSelective(record);
  }

  @Test
  void selectByPrimaryKeyDelegatesToMapper() {
    Long userId = 10L;
    OnlineShoppingUser expected = buildUser();
    when(onlineShoppingUserMapper.selectByPrimaryKey(userId)).thenReturn(expected);

    OnlineShoppingUser result = onlineShoppingUserDao.selectByPrimaryKey(userId);

    assertEquals(expected, result);
    verify(onlineShoppingUserMapper).selectByPrimaryKey(userId);
  }

  @Test
  void updateByPrimaryKeySelectiveDelegatesToMapper() {
    OnlineShoppingUser record = buildUser();
    when(onlineShoppingUserMapper.updateByPrimaryKeySelective(record)).thenReturn(1);

    int result = onlineShoppingUserDao.updateByPrimaryKeySelective(record);

    assertEquals(1, result);
    verify(onlineShoppingUserMapper).updateByPrimaryKeySelective(record);
  }

  @Test
  void updateByPrimaryKeyDelegatesToMapper() {
    OnlineShoppingUser record = buildUser();
    when(onlineShoppingUserMapper.updateByPrimaryKey(record)).thenReturn(1);

    int result = onlineShoppingUserDao.updateByPrimaryKey(record);

    assertEquals(1, result);
    verify(onlineShoppingUserMapper).updateByPrimaryKey(record);
  }

  private OnlineShoppingUser buildUser() {
    return OnlineShoppingUser.builder()
        .userId(10L)
        .userType(1)
        .name("Ada")
        .email("ada@example.com")
        .address("Paris")
        .phone("1234567890")
        .build();
  }
}
