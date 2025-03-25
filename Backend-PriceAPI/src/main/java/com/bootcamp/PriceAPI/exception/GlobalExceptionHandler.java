package com.bootcamp.PriceAPI.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.bootcamp.PriceAPI.DTO.StockDataDTO;
import com.bootcamp.PriceAPI.model.ApiResp;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(value = BusinessException.class)
  public ApiResp<StockDataDTO> handleBusinessException(BusinessException e) {
    return new ApiResp<StockDataDTO>(999999L, "Stock Code Not Found", null);
  }

  @ExceptionHandler(value = JsonProcessingException.class)
  public String handleJsonProcessingException(JsonProcessingException e) {
    return "Stock Code Not Found";
  }
}
