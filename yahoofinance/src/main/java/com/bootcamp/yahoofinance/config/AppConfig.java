package com.bootcamp.yahoofinance.config;

import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    // Ignore 404 errors to access response headers even on failure
    restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
      @Override
      public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode() != HttpStatus.NOT_FOUND) {
          super.handleError(response); // Throw exception for non-404 errors
        }
      }
    });
    return restTemplate;
  }
}