package com.nightfriday.onlineshopping.controller;

import com.nightfriday.onlineshopping.db.dao.OnlineShoppingCommodityDao;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingCommodity;
import com.nightfriday.onlineshopping.db.po.OnlineShoppingOrder;
import com.nightfriday.onlineshopping.services.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Slf4j
public class OrderController {
  @Autowired private OrderService orderService;
  @Autowired private OnlineShoppingCommodityDao commodityDao;

  @RequestMapping("/commodity/buy/{userId}/{commodityId}")
  public ModelAndView buyCommodity(@PathVariable Long userId, @PathVariable Long commodityId) {
    ModelAndView modelAndView = new ModelAndView();
    // log.info("User {} is attempting to buy commodity {}", userId, commodityId);
    OnlineShoppingOrder order = orderService.createOnlineShoppingOrderFinal(userId, commodityId);
    if (order != null) {
      modelAndView.addObject(
          "resultInfo", "Order created successfully, orderNo: " + order.getOrderNo());
      modelAndView.addObject("orderNo", order.getOrderNo());
    } else {
      modelAndView.addObject(
          "resultInfo",
          "Order creation failed" + commodityId);
    }
    modelAndView.setViewName("order_result");
    return modelAndView;
  }
  @RequestMapping("/commodity/orderQuery/{orderNo}")
  public ModelAndView orderQuery(@PathVariable String orderNo) {
    log.info("Order inquiry with orderNo: " + orderNo);
    OnlineShoppingOrder order = orderService.getByOrderNum(orderNo);
    ModelAndView modelAndView = new ModelAndView();
    if (order != null) {
      modelAndView.setViewName("order_check");
      modelAndView.addObject("order", order);
      OnlineShoppingCommodity commodity = commodityDao.selectByPrimaryKey(order.getCommodityId());
      modelAndView.addObject("commodity", commodity);
    } else {
      modelAndView.setViewName("error");
    }
    return modelAndView;
  }
  @RequestMapping("/commodity/payOrder/{orderNumber}")
  public String payOrder(@PathVariable String orderNumber) {
    log.info("Pay order with orderNumber: " + orderNumber);
    orderService.payOrderProcess(orderNumber);
    return "redirect:/commodity/orderQuery/" + orderNumber;
  }
}
