package com.reliaquest.api.controller;

import com.reliaquest.api.exceptions.EmployeeNotFoundException;
import com.reliaquest.api.service.ApiV1Service;
import com.reliaquest.server.model.DeleteMockEmployeeInput;
import com.reliaquest.server.model.MockEmployee;
import com.reliaquest.server.model.CreateMockEmployeeInput;
import com.reliaquest.server.model.Response;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v2/employee")
public class IEmployeeControllerImpl implements IEmployeeController<MockEmployee, CreateMockEmployeeInput>{

    private final ApiV1Service apiV1Service;

    public IEmployeeControllerImpl(ApiV1Service apiV1Service) {
        this.apiV1Service = apiV1Service;
    }

    @Override
    @GetMapping
    public ResponseEntity<List<MockEmployee>> getAllEmployees() {
        try{
            List<MockEmployee> employeeList = apiV1Service.getAllEmployeeList();
            return ResponseEntity.ok(employeeList);
        } catch (RuntimeException e){
            log.error("Exception occurred while fetching employee list", e);
            throw new EmployeeNotFoundException("Employees not found");
        }
    }

    @GetMapping("/search/{searchString}")
    @Override
    public ResponseEntity<List<MockEmployee>> getEmployeesByNameSearch(@PathVariable("searchString") String searchString) {
        try{
            List<MockEmployee> employeeList = apiV1Service.getAllEmployeeList();
            List<MockEmployee> filteredEmployeeList = employeeList.stream()
                    .filter(mockEmployee -> mockEmployee.getName().contains(searchString)).collect(Collectors.toList());
            return ResponseEntity.ok(filteredEmployeeList);
        } catch (RuntimeException e){
            log.error("Exception occurred while fetching employees with searchString:{}", searchString, e);
            throw new EmployeeNotFoundException("Any employee does not match the provided criteria");
        }
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<MockEmployee> getEmployeeById(@PathVariable("id") String id) {
        try{
           return ResponseEntity.ok(apiV1Service.getEmployeeById(UUID.fromString(id)));
        } catch (RuntimeException e){
            log.error("Exception occurred while fetching employee with id:{}", id, e);
            throw new EmployeeNotFoundException(String.format("Employee not found for id %s", id));
        }
    }

    @GetMapping("/highestSalary")
    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        try{
            List<MockEmployee> employeeList = apiV1Service.getAllEmployeeList();
            return employeeList.stream()
                    .max(Comparator.comparingInt(MockEmployee::getSalary))
                    .map(employee -> ResponseEntity.ok(employee.getSalary()))
                   .orElseThrow(RuntimeException::new);
        } catch (RuntimeException e){
            log.error("Exception occurred while fetching employee with highest salary", e);
            throw new EmployeeNotFoundException("Employees not found");
        }
    }

    @GetMapping("/topTenHighestEarningEmployeeNames")
    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        try{
            List<MockEmployee> employeeList = apiV1Service.getAllEmployeeList();
            List<String> filteredEmployeeNames = employeeList.stream()
                    .sorted(Comparator.comparingInt(MockEmployee::getSalary).reversed())
                    .limit(10)
                    .map(MockEmployee::getName)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(filteredEmployeeNames);
        } catch (RuntimeException e){
            log.error("Exception occurred while fetching top 10 highest salary", e);
            throw new EmployeeNotFoundException("Employees not found");
        }

    }

    @PostMapping()
    @Override
    public ResponseEntity<MockEmployee> createEmployee(@Valid @RequestBody CreateMockEmployeeInput employee) {
        try{
            return ResponseEntity.ok(apiV1Service.createEmployee(employee));
        } catch(RuntimeException e){
            log.error("Exception occurred while creating employee with name:{}", employee.getName(), e);
            throw new IllegalArgumentException("Request was unsuccessful, please try again");
        }
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        try{
            MockEmployee employee = apiV1Service.getEmployeeById(UUID.fromString(id));
            DeleteMockEmployeeInput deleteMockEmployeeInput = new DeleteMockEmployeeInput();
            deleteMockEmployeeInput.setName(employee.getName());
            apiV1Service.deleteEmployee(deleteMockEmployeeInput);
            return ResponseEntity.ok(employee.getName());
        } catch(RuntimeException e){
            log.error("Exception occurred while deleting employee with id:{}", id, e);
            throw new EmployeeNotFoundException(String.format("Employee not found for id %s, it might be already deleted", id));
        }
    }
}
