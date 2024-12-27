package com.reliaquest.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.server.model.CreateMockEmployeeInput;
import com.reliaquest.server.model.DeleteMockEmployeeInput;
import com.reliaquest.server.model.MockEmployee;
import com.reliaquest.server.model.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class ApiV1Service {

    private final WebClient apiV1WebClient;

    public ApiV1Service(@Qualifier("apiV1WebClient") WebClient apiV1WebClient) {
        this.apiV1WebClient = apiV1WebClient;
    }

    @SneakyThrows
    public List<MockEmployee> getAllEmployeeList(){
        Mono<DataBuffer> dataBufferMono = apiV1WebClient.get()
                .exchangeToMono(clientResponse -> {
                    if(clientResponse.statusCode().equals(HttpStatus.OK)){
                        return clientResponse.bodyToMono(DataBuffer.class);
                    } else{
                        return Mono.error(() -> new RuntimeException("V1 employee API didn't return successful response, status code : " + clientResponse.statusCode()));
                    }
                });

        Mono<InputStream> resp = DataBufferUtils
                .join(dataBufferMono)
                .publishOn(Schedulers.boundedElastic())
                .map(dataBuffer -> dataBuffer.asInputStream(true));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(resp.block(), new TypeReference<List<MockEmployee>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error occurred during response conversion to MockEmployee object", e);
        }
    }

    public MockEmployee getEmployeeById(UUID id){
        RestTemplate restTemplate = new RestTemplate();
        URI uri = UriComponentsBuilder.fromHttpUrl("http://localhost:8112/api/v1/employee").path("/{id}").build(id);

        MockEmployee employee = new MockEmployee();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MockEmployee> entity = new HttpEntity<MockEmployee>(employee, headers);
        ResponseEntity<Response<MockEmployee>> result = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<Response<MockEmployee>>() {});
        return Objects.requireNonNull(result.getBody()).data();
    }

    public MockEmployee createEmployee(CreateMockEmployeeInput employee){
        return apiV1WebClient.post()
                .body(Mono.just(employee), CreateMockEmployeeInput.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(clientResponse -> {
                    if(clientResponse.statusCode().equals(HttpStatus.OK)){
                        return clientResponse.bodyToMono(MockEmployee.class);
                    } else{
                        return Mono.error(() -> new RuntimeException("Exception occurred while creating employee, status code : " + clientResponse.statusCode()));
                    }
                })
               .block();
    }

    public void deleteEmployee(DeleteMockEmployeeInput deleteMockEmployeeInput){
        Boolean employeeDeleted = apiV1WebClient
                .method(HttpMethod.DELETE)
                .body(Mono.just(deleteMockEmployeeInput), DeleteMockEmployeeInput.class)
                .exchangeToMono(clientResponse -> {
                    if(clientResponse.statusCode().equals(HttpStatus.OK)){
                        return clientResponse.bodyToMono(Boolean.class);
                    } else{
                        return Mono.error(() -> new RuntimeException("Exception occurred while deleting employee, status code : " + clientResponse.statusCode()));
                    }
                })
                .block();
        if(Boolean.FALSE.equals(employeeDeleted)){
            log.error("Employee with name {} might be already deleted.", deleteMockEmployeeInput.getName());
        }
    }
}
