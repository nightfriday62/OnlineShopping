package com.nightfriday.onlineshopping.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  private final Dependency dependency;

  public HelloController(Dependency dependency) {
    this.dependency = dependency;
  }

  @RequestMapping("/hello")
  public String hello() {
    return "Hello, World!";
  }

  @RequestMapping("/echo/{message}")
  public String echo(@PathVariable String message) {
    return "Hello from you" + message;
  }

  public int addPlus2(int a, int b) {
    return dependency.add(a, b) + 2;
  }
}
