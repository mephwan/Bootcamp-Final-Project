package com.bootcamp.yahoofinance.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(value = BusinessException.class)
  public String handleBusinessException(BusinessException e) {
    return "Stock Code Not Found";
  }
}
