package com.hillel.ua.graphql.repository;

import com.hillel.ua.graphql.entities.Department;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface DepartmentRepository extends
        CrudRepository<Department, Integer>, JpaSpecificationExecutor<Department> {

    Department findDepartmentByNameContainingIgnoreCase(String name);
}
