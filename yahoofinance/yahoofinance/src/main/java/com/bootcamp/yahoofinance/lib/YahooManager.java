package com.bootcamp.yahoofinance.lib;

import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.bootcamp.yahoofinance.model.YahooDto;
import com.bootcamp.yahoofinance.util.Yahoo;

@Component
public class YahooManager {

  public YahooDto getYahooDtos(RestTemplate restTemplate,
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


        //System.out.println(tryurl);
    //String url =
    //    "https://query1.finance.yahoo.com/v7/finance/quote?symbols=0388.HK&crumb="
    //        + crumb;

    ResponseEntity<YahooDto> responseDto = restTemplate.exchange(tryurl,
        HttpMethod.GET, new HttpEntity<>(headers), YahooDto.class);

    return responseDto.getBody();
  }
}

