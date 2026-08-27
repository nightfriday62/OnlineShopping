package com.nightfriday.onlineshopping.controller;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class Dependency {
  public int add(int a, int b) {
    return a + b;
  }
}
