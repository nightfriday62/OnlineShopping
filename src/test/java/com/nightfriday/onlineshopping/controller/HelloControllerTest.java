package com.nightfriday.onlineshopping.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class HelloControllerTest {

  @Spy private Dependency dependency;
  @InjectMocks private HelloController helloController;

  @BeforeEach
  void setUp() {
    helloController = new HelloController(dependency);
  }

  @Test
  void addPlus2WithSpy() {
    int result = helloController.addPlus2(1, 2);
    assertEquals(5, result);
  }

  @Test
  void addPlus2() {
    when(dependency.add(anyInt(), anyInt())).thenReturn(5);
    int result = helloController.addPlus2(1, 2);
    assertEquals(7, result);
  }
}
