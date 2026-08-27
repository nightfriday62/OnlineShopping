package com.nightfriday.onlineshopping.persistence;

import lombok.Data;

@Data
public class User {
  private String id;
  private String name;
  private String email;
  private String password;
}
