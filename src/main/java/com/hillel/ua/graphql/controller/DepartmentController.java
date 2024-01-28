package com.hillel.ua.graphql.controller;

import com.hillel.ua.graphql.dto.DepartmentRequestDto;
import com.hillel.ua.graphql.dto.EmployeeRequestDto;
import com.hillel.ua.graphql.entities.Department;
import com.hillel.ua.graphql.entities.Employee;
import com.hillel.ua.graphql.entities.Organization;
import com.hillel.ua.graphql.repository.DepartmentRepository;
import com.hillel.ua.graphql.repository.EmployeeRepository;
import com.hillel.ua.graphql.repository.OrganizationRepository;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;

    private final EmployeeRepository employeeRepository;

    public DepartmentController(DepartmentRepository departmentRepository, OrganizationRepository organizationRepository,
                                EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.organizationRepository = organizationRepository;
        this.employeeRepository = employeeRepository;
    }

    @MutationMapping
    public Department newDepartment(@Argument DepartmentRequestDto department) {
        Organization organization = organizationRepository.findById(department.getOrganizationId()).orElseThrow(() -> new RuntimeException("Organization not found"));
        return departmentRepository.save(new Department(null, department.getName(), null, organization));
    }

    @QueryMapping
    public Iterable<Department> departments(DataFetchingEnvironment environment) {


 //       DataFetchingFieldSelectionSet selectionSet = environment.getSelectionSet();
 //       System.out.println(selectionSet);
 //       List<Specification<Department>> specifications = buildSpecifications(selectionSet);
 //       return departmentRepository.findAll(Specification.where(specifications.stream().reduce(Specification::and).orElse(null)));
        return departmentRepository.findAll();
    }

    @QueryMapping
    public Department department(@Argument Integer id, DataFetchingEnvironment environment) {
        Specification<Department> spec = byId(id);
        DataFetchingFieldSelectionSet selectionSet = environment.getSelectionSet();
        if (selectionSet.contains("employees")) spec = spec.and(fetchEmployees());
        if (selectionSet.contains("organization")) spec = spec.and(fetchOrganization());
        return departmentRepository.findOne(spec).orElseThrow(() -> new RuntimeException("Department not found"));
    }

    @QueryMapping
    public Department departmentByName(@Argument String name, DataFetchingEnvironment environment) {
       Department  dep =  departmentRepository.findDepartmentByNameContainingIgnoreCase(name);
        if(dep == null)
            throw new EntityNotFoundException("Department containing "+ name +" is not found");
        return dep;
    }

    @MutationMapping
    public Department updateDepartment(@Argument Integer id, @Argument DepartmentRequestDto department) {
        Department departmentToUpd = departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Department not found"));
        departmentToUpd.setName(department.getName());

        return departmentRepository.save(departmentToUpd);
    }

    @MutationMapping
    public String deleteDepartment(@Argument Integer id) {
        Department departmentToUpd = departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Department not found"));
        departmentRepository.delete(departmentToUpd);
        return "Department with id: "+ id+ " was successfully deleted!";
    }

    @MutationMapping
    public Department addEmployee(@Argument Integer id, @Argument EmployeeRequestDto employee) {
        Department departmentToUpd = departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Department not found"));
        Employee addedEmpl = new Employee();
        addedEmpl.setDepartment(departmentRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Department not found")
        ));
        addedEmpl.setAge(employee.getAge());
        addedEmpl.setOrganization(organizationRepository.findById(employee.getOrganizationId()).orElseThrow(
                () -> new RuntimeException("Organization not found")
        ));
        addedEmpl.setPosition(employee.getPosition());
        addedEmpl.setFirstName(employee.getFirstName());
        addedEmpl.setLastName(employee.getLastName());
        addedEmpl.setSalary(employee.getSalary());
        employeeRepository.save(addedEmpl);
        departmentToUpd.getEmployees().add(addedEmpl);
        departmentRepository.save(departmentToUpd);
        return departmentRepository.findById(id).orElse(null);
    }

    @BatchMapping
    Map<Department, List<Employee>> employee(List<Department> departments){
        System.out.println("Calling batch");
        return departments
                .stream()
                .collect(Collectors.toMap(department -> department,
                        department->employeeRepository.findEmployeesByDepartment(department)));
    }

    private List<Specification<Department>> buildSpecifications(DataFetchingFieldSelectionSet selectionSet) {
        return List.of(selectionSet.contains("employees") ? fetchEmployees() : null, selectionSet.contains("organization") ? fetchOrganization() : null);
    }

 //   @SchemaMapping(typeName = "Department")
//    private Employee getEmployee(Employee empl){
 //       return
 //   }

    private Specification<Department> fetchEmployees() {
        return (root, query, builder) -> {
            root.fetch("employees", JoinType.LEFT);
            return builder.isNotEmpty(root.get("employees"));
        };
    }

    private Specification<Department> fetchOrganization() {
        return (root, query, builder) -> {
            root.fetch("organization", JoinType.LEFT);
            return builder.isNotNull(root.get("organization"));
        };
    }

    private Specification<Department> byId(Integer id) {
        return (root, query, builder) -> builder.equal(root.get("id"), id);
    }

}
