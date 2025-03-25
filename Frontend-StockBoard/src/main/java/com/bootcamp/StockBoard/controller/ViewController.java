package com.bootcamp.StockBoard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
  
  @GetMapping(value = "/chart")
  public String showchart(Model model) {

    return "chart";
  }
}
