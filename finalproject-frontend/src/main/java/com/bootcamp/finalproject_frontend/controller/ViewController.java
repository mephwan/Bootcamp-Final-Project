package com.bootcamp.finalproject_frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
  
  @GetMapping(value = "/candlechart")
  public String candlechart(Model model) {

    return "candlechart";
  }
}
