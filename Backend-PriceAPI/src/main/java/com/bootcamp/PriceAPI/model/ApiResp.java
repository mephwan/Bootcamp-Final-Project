package com.bootcamp.PriceAPI.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResp<T> {
  private Long code;
  private String message;
  private T body;  
}
