package com.github.devraghav.bugtracker.issue.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

@Configuration
public class UserWebClientConfiguration {
  @Bean
  public WebClient userWebClient(WebClient.Builder webClientBuilder) {

    HttpClient httpClient =
        HttpClient.create()
            .protocol(HttpProtocol.H2C)
            .wiretap(true)
            // it is the time we wait to receive a response after sending a
            // request.
            .responseTimeout(Duration.ofMillis(500))
            // Period within which a connection between a client and a server must be established
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1_000);

    return webClientBuilder
        .defaultHeader(HttpHeaders.USER_AGENT, "project-service")
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
  }
}
