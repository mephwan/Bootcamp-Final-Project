package com.bootcamp.PriceAPI.model;

import java.util.List;
import lombok.Getter;

@Getter
public class YahooOHLCDto {
  private Chart chart;

  @Getter
  public static class Chart {
    private List<Result> result;
    private String error;

    @Getter
    public static class Result {
      private Meta meta;
      private List<Long> timestamp;
      private Indicators indicators;

      @Getter
      public static class Indicators {
        private List<Quote> quote;
        private List<Adjclose> adjclose;

        @Getter
        public static class Quote {
          private List<Double> high;
          private List<Double> open;
          private List<Double> low;
          private List<Long> volume;
          private List<Double> close;
        }

        @Getter
        public static class Adjclose {
          private List<Double> adjclose;
        }
      }

      @Getter
      public static class Meta {
        private String currency;
        private String symbol;
        private String exchangeName;
        private String fullExchangeName;
        private String instrumentType;
        private Long firstTradeDate;
        private Long regularMarketTime;
        private Boolean hasPrePostMarketData;
        private Long gmtoffset;
        private String timezone;
        private String exchangeTimezoneName;
        private Double regularMarketPrice;
        private Double fiftyTwoWeekHigh;
        private Double fiftyTwoWeekLow;
        private Double regularMarketDayHigh;
        private Double regularMarketDayLow;
        private Long regularMarketVolume;
        private String longName;
        private String shortName;
        private Double chartPreviousClose;
        private Double priceHint;
        private CurrentTradingPeriod currentTradingPeriod;

        @Getter
        public static class CurrentTradingPeriod {
          private TradingPeriod pre;
          private TradingPeriod regular;
          private TradingPeriod post;

          @Getter
          public static class TradingPeriod {
            private String timezone;
            private Long end;
            private Long start;
            private Long gmtoffset;
          }
        }

        private String dataGranularity;
        private String range;
        private List<String> validRanges;


      }
    }
  }
}
