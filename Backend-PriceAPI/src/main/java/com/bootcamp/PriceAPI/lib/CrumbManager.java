package com.bootcamp.PriceAPI.lib;

import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.bootcamp.PriceAPI.util.Yahoo;
import lombok.Getter;

@Getter
public class CrumbManager {
  private RestTemplate restTemplate;
  private HttpHeaders headers;

  public CrumbManager(RestTemplate restTemplate, HttpHeaders headers) {
    this.restTemplate = restTemplate;
    this.headers = headers;
  }

  public String getCrumb(HttpHeaders headers, String cookieHeader) {

    this.headers.set("Cookie", cookieHeader);
    this.headers.set("User-Agent", "Mozilla/5.0");

    String url = UriComponentsBuilder.newInstance().scheme("https")
        .host(Yahoo.DOMAIN).path(Yahoo.VERSION_CRUMB).path(Yahoo.ENDPOINT_CRUMB)
        .build().toUriString();

    ResponseEntity<String> response = null;

    while (response == null) {
      try {

        response = restTemplate.exchange(url, HttpMethod.GET,
            new HttpEntity<>(this.headers), String.class);
      } catch (HttpClientErrorException e) {

      }

      try {
        TimeUnit.SECONDS.sleep(5);
      } catch (InterruptedException e) {
        System.out.println("Retry......");
      }
    }

    return response.getBody(); // Returns the crumb string
  }
}
