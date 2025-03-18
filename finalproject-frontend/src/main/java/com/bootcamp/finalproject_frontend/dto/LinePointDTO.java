package com.bootcamp.finalproject_frontend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LinePointDTO {
  private Long dateTime;
  private Double close;
}
