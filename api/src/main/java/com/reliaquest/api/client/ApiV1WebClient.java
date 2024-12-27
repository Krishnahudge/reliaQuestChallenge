package com.reliaquest.api.client;

import com.reliaquest.api.config.ApiV1Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApiV1WebClient {

    private final ApiV1Config apiV1Config;

    public ApiV1WebClient(ApiV1Config apiV1Config) {
        this.apiV1Config = apiV1Config;
    }

    @Bean("apiV1WebClient")
    public WebClient getApiV1WebClient(){
        return WebClient.create(apiV1Config.getEndpoint());
    }
}
