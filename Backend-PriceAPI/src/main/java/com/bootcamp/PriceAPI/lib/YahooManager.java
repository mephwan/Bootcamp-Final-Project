package com.bootcamp.PriceAPI.lib;

import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.bootcamp.PriceAPI.model.YahooStockPriceDto;
import com.bootcamp.PriceAPI.model.YahooOHLCDto;
import com.bootcamp.PriceAPI.util.Yahoo;

@Component
public class YahooManager {

  public YahooStockPriceDto getYahooDtos(RestTemplate restTemplate,
      List<String> stockList) {

    CookieManager cookieManager = new CookieManager(restTemplate);
    String cookieHeader = cookieManager.getCookie();
    restTemplate = cookieManager.getRestTemplate();
    HttpHeaders headers = cookieManager.getHeaders();

    CrumbManager crumbManager = new CrumbManager(restTemplate, headers);
    String crumb = crumbManager.getCrumb(headers, cookieHeader);
    restTemplate = crumbManager.getRestTemplate();
    headers = crumbManager.getHeaders();

    String params = "symbols=";

    for (String stock : stockList) {
      params += stock + ",";
    }

    params = params.substring(0, params.length() - 1);
    params += "&crumb=" + crumb;

    String tryurl = UriComponentsBuilder.newInstance().scheme("https")
        .host(Yahoo.DOMAIN).path(Yahoo.VERSION_QUOTE).path(Yahoo.ENDPOINT_QUOTE)
        .query(params).toUriString();

    ResponseEntity<YahooStockPriceDto> responseDto = restTemplate.exchange(tryurl,
        HttpMethod.GET, new HttpEntity<>(headers), YahooStockPriceDto.class);

    return responseDto.getBody();
  }

  public YahooOHLCDto getOhlcDto(RestTemplate restTemplate, String stockcode,
      Long fromTimeStamp, Long toTimeStamp) {
    CookieManager cookieManager = new CookieManager(restTemplate);
    String cookieHeader = cookieManager.getCookie();
    restTemplate = cookieManager.getRestTemplate();
    HttpHeaders headers = cookieManager.getHeaders();

    CrumbManager crumbManager = new CrumbManager(restTemplate, headers);
    String crumb = crumbManager.getCrumb(headers, cookieHeader);
    restTemplate = crumbManager.getRestTemplate();
    headers = crumbManager.getHeaders();

    String url = UriComponentsBuilder.newInstance().scheme("https") //
    .host(Yahoo.DOMAIN).path(Yahoo.VERSION_CHART).path(Yahoo.ENDPOINT_CHART) //
    .build().toUriString() + stockcode + "?period1=" + fromTimeStamp //
    + "&period2=" + toTimeStamp + "&interval=1d&events=history&crumb="
            + crumb;


    ResponseEntity<YahooOHLCDto> responseDto = restTemplate.exchange(url,
        HttpMethod.GET, new HttpEntity<>(headers), YahooOHLCDto.class);

    return responseDto.getBody();
  }
}

