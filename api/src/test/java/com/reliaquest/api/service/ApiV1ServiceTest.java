package com.reliaquest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.server.model.CreateMockEmployeeInput;
import com.reliaquest.server.model.DeleteMockEmployeeInput;
import com.reliaquest.server.model.MockEmployee;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ApiV1ServiceTest {

    private static MockWebServer mockBackEnd;

    private static final  ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    public void testGetAllEmployeeList() throws JsonProcessingException, InterruptedException {
        WebClient testWebClient = WebClient.create(mockBackEnd.url("/").url().toString());
        ApiV1Service apiV1Service = new ApiV1Service(testWebClient);
        //Here we can create actual list of mock objects as per the response of "http://localhost:8112/api/v1/employee"
        List<MockEmployee> expectedMockEmployeeList = new ArrayList<>(50);
        String response = OBJECT_MAPPER.writeValueAsString(expectedMockEmployeeList);

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        List<MockEmployee> actualMockEmployeeList = apiV1Service.getAllEmployeeList();
        Assertions.assertEquals(expectedMockEmployeeList.size(), actualMockEmployeeList.size());
        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        Assertions.assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    public void testGetAllEmployeeList_errorInResponse() throws InterruptedException {
        WebClient testWebClient = WebClient.create(mockBackEnd.url("/").url().toString());
        ApiV1Service apiV1Service = new ApiV1Service(testWebClient);

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "application/json");

        mockBackEnd.enqueue(mockResponse);
        Exception exception = Assertions.assertThrows(RuntimeException.class, apiV1Service::getAllEmployeeList);
        Assertions.assertEquals("V1 employee API didn't return successful response, status code : 500", exception.getMessage());
        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        Assertions.assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    public void testCreateEmployee() throws JsonProcessingException, InterruptedException {
        WebClient testWebClient = WebClient.create(mockBackEnd.url("/").url().toString());
        ApiV1Service apiV1Service = new ApiV1Service(testWebClient);
        CreateMockEmployeeInput employeeInput = new CreateMockEmployeeInput();
        employeeInput.setName("abc");
        employeeInput.setSalary(50000);
        MockEmployee expectedmMockEmployee = new MockEmployee();
        expectedmMockEmployee.setName("abc");
        expectedmMockEmployee.setSalary(50000);
        String response = OBJECT_MAPPER.writeValueAsString(expectedmMockEmployee);

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        MockEmployee actualMockEmployee = apiV1Service.createEmployee(employeeInput);
        assertThat(actualMockEmployee)
                .usingRecursiveComparison()
                .isEqualTo(expectedmMockEmployee);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        Assertions.assertEquals("POST", recordedRequest.getMethod());
    }

    @Test
    public void testCreateEmployee_errorInResponse() throws InterruptedException {
        WebClient testWebClient = WebClient.create(mockBackEnd.url("/").url().toString());
        ApiV1Service apiV1Service = new ApiV1Service(testWebClient);
        CreateMockEmployeeInput employeeInput = new CreateMockEmployeeInput();

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "application/json");

        mockBackEnd.enqueue(mockResponse);

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> apiV1Service.createEmployee(employeeInput));
        Assertions.assertEquals("Exception occurred while creating employee, status code : 500", exception.getMessage());
        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        Assertions.assertEquals("POST", recordedRequest.getMethod());
    }

    @Test
    public void testDeleteEmployee() throws InterruptedException {
        WebClient testWebClient = WebClient.create(mockBackEnd.url("/").url().toString());
        ApiV1Service apiV1Service = new ApiV1Service(testWebClient);

        DeleteMockEmployeeInput deleteMockEmployeeInput = new DeleteMockEmployeeInput();
        deleteMockEmployeeInput.setName("abc");

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(Boolean.TRUE.toString())
                .addHeader("Content-Type", "application/json"));

        apiV1Service.deleteEmployee(deleteMockEmployeeInput);
        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        Assertions.assertEquals("DELETE", recordedRequest.getMethod());
    }

    @Test
    public void testDeleteEmployee_errorInResponse() throws InterruptedException {
        WebClient testWebClient = WebClient.create(mockBackEnd.url("/").url().toString());
        ApiV1Service apiV1Service = new ApiV1Service(testWebClient);

        DeleteMockEmployeeInput deleteMockEmployeeInput = new DeleteMockEmployeeInput();
        deleteMockEmployeeInput.setName("abc");

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "application/json"));

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> apiV1Service.deleteEmployee(deleteMockEmployeeInput));
        Assertions.assertEquals("Exception occurred while deleting employee, status code : 500", exception.getMessage());
        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        Assertions.assertEquals("DELETE", recordedRequest.getMethod());
    }
}
