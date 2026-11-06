package com.mentor.orderservice.client;

import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class TicketClientConfig {

    @Bean
    RestClient ticketRestClient(RestClient.Builder builder, TicketServiceProperties properties) {
        // HTTP/1.1 avoids the JDK client's HTTP/2 upgrade attempt, which some servers answer badly
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());

        return builder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .requestInterceptor(new JwtTokenRelayInterceptor())
                .build();
    }
}