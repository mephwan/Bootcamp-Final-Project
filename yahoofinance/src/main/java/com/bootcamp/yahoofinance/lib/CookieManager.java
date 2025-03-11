package com.bootcamp.yahoofinance.lib;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.bootcamp.yahoofinance.util.Yahoo;
import lombok.Getter;

@Getter
@Component
public class CookieManager {
  private RestTemplate restTemplate;
  private HttpHeaders headers;

  @Value("${api.cookie}")
  private String cookieDomain;


  public CookieManager(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
    this.headers = new HttpHeaders();
  }

  public String getCookie() {
    System.out.println(cookieDomain);
    this.headers.set("User-Agent", "Mozilla/5.0");
    this.headers.set("Referer", "https://finance.yahoo.com/");

    String url = UriComponentsBuilder.newInstance().scheme("https")
    .host(Yahoo.DOMAIN_COOKIE).build().toUriString();

    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET,
            new HttpEntity<>(this.headers), String.class);

    List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);

    // Parse cookies into "key=value" format
    String cookieHeader =
        cookies.stream().map(cookie -> cookie.split(";")[0].trim()) // Extract key=value
            .collect(Collectors.joining("; "));

    return cookieHeader;
  }

}
