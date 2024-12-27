package com.reliaquest.api.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("webclient.api.v1")
public class ApiV1Config {
    private String endpoint = "http://localhost:8112/api/v1/employee";
}
