package com.bootcamp.finalproject_frontend.model;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class LinePoint {
  private LocalDateTime dateTime;
  private Double close;

  public LinePoint(int year, int month, int dayOfMonth, int hour, int minute,
      Double close) {
    this.dateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute, 0);
    this.close = close;
  }

  public enum TYPE {
    FIVE_MIN;

    public static LinePoint.TYPE of(String type) {
      return switch (type) {
        case "5m" -> LinePoint.TYPE.FIVE_MIN;
        default -> throw new RuntimeException();
      };
    }
  }
}
