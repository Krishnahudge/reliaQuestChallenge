package com.reliaquest.api.controller;

import com.reliaquest.api.exceptions.EmployeeNotFoundException;
import com.reliaquest.api.service.ApiV1Service;
import com.reliaquest.server.model.CreateMockEmployeeInput;
import com.reliaquest.server.model.DeleteMockEmployeeInput;
import com.reliaquest.server.model.MockEmployee;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@ExtendWith(MockitoExtension.class)
public class IEmployeeControllerImplTest {

    @Mock
    private ApiV1Service apiV1Service;

    @InjectMocks
    private IEmployeeControllerImpl iEmployeeController;

    @Test
    public void testGetAllEmployees(){
        List<MockEmployee> mockEmployeeList = new ArrayList<>(50);
        org.mockito.Mockito.when(apiV1Service.getAllEmployeeList()).thenReturn(mockEmployeeList);

        List<MockEmployee> actualMockEmployeeList = iEmployeeController.getAllEmployees().getBody();
        Assertions.assertNotNull(actualMockEmployeeList);
        Assertions.assertEquals(mockEmployeeList.size(), actualMockEmployeeList.size());
    }

    @Test
    public void testGetAllEmployees_withError(){
        org.mockito.Mockito.when(apiV1Service.getAllEmployeeList()).thenThrow(new RuntimeException());
        Assertions.assertThrows(EmployeeNotFoundException.class,
                () -> iEmployeeController.getAllEmployees(), "Employees not found");
    }

    @Test
    public void testGetEmployeesByNameSearch(){
        MockEmployee mockEmployee = new MockEmployee();
        mockEmployee.setName("abc");
        List<MockEmployee> mockEmployeeList = Arrays.asList(mockEmployee);
        org.mockito.Mockito.when(apiV1Service.getAllEmployeeList()).thenReturn(mockEmployeeList);

        List<MockEmployee> actualMockEmployeeList = iEmployeeController.getEmployeesByNameSearch("a").getBody();
        Assertions.assertEquals(1, actualMockEmployeeList.size());
        Assertions.assertEquals("abc", actualMockEmployeeList.get(0).getName());
    }

    @Test
    public void testGetEmployeesByNameSearch_WithError(){
        org.mockito.Mockito.when(apiV1Service.getAllEmployeeList()).thenThrow(new RuntimeException());
        Assertions.assertThrows(EmployeeNotFoundException.class,
                () -> iEmployeeController.getEmployeesByNameSearch("a"), "Any employee does not match the provided criteria");
    }

    @Test
    public void testGetEmployeeById(){
        UUID id = UUID.randomUUID();
        MockEmployee mockEmployee = new MockEmployee();
        mockEmployee.setId(id);
        org.mockito.Mockito.when(apiV1Service.getEmployeeById(id)).thenReturn(mockEmployee);

        MockEmployee actualMockEmployee = iEmployeeController.getEmployeeById(id.toString()).getBody();
        Assertions.assertNotNull(actualMockEmployee);
        Assertions.assertEquals(id, actualMockEmployee.getId());
    }

    @Test
    public void testGetEmployeeById_WithError(){
        UUID id = UUID.randomUUID();
        org.mockito.Mockito.when(apiV1Service.getEmployeeById(id)).thenThrow(new RuntimeException());
        Assertions.assertThrows(EmployeeNotFoundException.class,
                () -> iEmployeeController.getEmployeeById(id.toString()), String.format("Employee not found for id %s", id));
    }

    @Test
    public void testGetHighestSalaryOfEmployees(){
        MockEmployee mockEmployee1 = new MockEmployee();
        mockEmployee1.setName("a");
        mockEmployee1.setSalary(10000);

        MockEmployee mockEmployee2 = new MockEmployee();
        mockEmployee1.setName("b");
        mockEmployee1.setSalary(20000);

        List<MockEmployee> mockEmployeeList = Arrays.asList(mockEmployee1, mockEmployee2);
        org.mockito.Mockito.when(apiV1Service.getAllEmployeeList()).thenReturn(mockEmployeeList);

        Integer highestSalary = iEmployeeController.getHighestSalaryOfEmployees().getBody();
        Assertions.assertNotNull(highestSalary);
        Assertions.assertEquals(20000, highestSalary.intValue());
    }

    @Test
    public void testGetHighestSalaryOfEmployees_WithError(){
        org.mockito.Mockito.when(apiV1Service.getAllEmployeeList()).thenThrow(new RuntimeException());
        Assertions.assertThrows(EmployeeNotFoundException.class,
                () -> iEmployeeController.getHighestSalaryOfEmployees(), "Employees not found");
    }

    @Test
    public void testCreateEmployee(){
        CreateMockEmployeeInput input = new CreateMockEmployeeInput();
        input.setName("abc");

        MockEmployee mockEmployee = new MockEmployee();
        mockEmployee.setName("abc");
        org.mockito.Mockito.when(apiV1Service.createEmployee(input)).thenReturn(mockEmployee);

        MockEmployee actualMockEmployee = iEmployeeController.createEmployee(input).getBody();
        Assertions.assertNotNull(actualMockEmployee);
        Assertions.assertEquals("abc", actualMockEmployee.getName());
    }

    @Test
    public void testCreateEmployee_WithError(){
        CreateMockEmployeeInput input = new CreateMockEmployeeInput();
        org.mockito.Mockito.when(apiV1Service.createEmployee(input)).thenThrow(new RuntimeException());
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> iEmployeeController.createEmployee(input), "Request was unsuccessful, please try again");
    }

    @Test
    public void testDeleteEmployeeById(){
        UUID id = UUID.randomUUID();
        MockEmployee mockEmployee = new MockEmployee();
        mockEmployee.setId(id);
        mockEmployee.setName("abc");

        org.mockito.Mockito.when(apiV1Service.getEmployeeById(id)).thenReturn(mockEmployee);

        DeleteMockEmployeeInput deleteMockEmployeeInput = new DeleteMockEmployeeInput();
        deleteMockEmployeeInput.setName("abc");

        String deletedEmployeeName = iEmployeeController.deleteEmployeeById(id.toString()).getBody();

        Assertions.assertNotNull(deletedEmployeeName);
        Assertions.assertEquals("abc", deletedEmployeeName);
    }

}
