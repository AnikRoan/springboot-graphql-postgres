package com.hillel.ua.graphql.controller;

import com.hillel.ua.graphql.dto.OrganizationRequestDto;
import com.hillel.ua.graphql.entities.Department;
import com.hillel.ua.graphql.entities.Organization;
import com.hillel.ua.graphql.entities.Employee;
import com.hillel.ua.graphql.repository.OrganizationRepository;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class OrganizationController {

    OrganizationRepository repository;

    OrganizationController(OrganizationRepository repository) {
        this.repository = repository;
    }

    @MutationMapping
    public Organization newOrganization(@Argument OrganizationRequestDto organization) {
        return repository.save(new Organization(null, organization.getName(), null, null));
    }

    @MutationMapping
    public Organization updOrganization(@Argument Integer id, @Argument OrganizationRequestDto organization){
        Organization orgToUpd = repository.findById(id).orElseThrow(() -> new RuntimeException("Organization not found"));
        orgToUpd.setName(organization.getName());
        return repository.save(orgToUpd);
    }

    @MutationMapping
    public String deleteOrganization(@Argument Integer id) {
        Organization orgToUpd = repository.findById(id).orElseThrow(() -> new RuntimeException("Organization not found"));
        repository.delete(orgToUpd);
        return "Organization with id: "+ id+ " was successfully deleted!";
    }

    @QueryMapping
    public Iterable<Organization> organizations() {
        return repository.findAll();
    }

    @QueryMapping
    public Organization organization(@Argument Integer id, DataFetchingEnvironment environment) {
        Specification<Organization> spec = byId(id);
        DataFetchingFieldSelectionSet selectionSet = environment
                .getSelectionSet();
        if (selectionSet.contains("employees"))
            spec = spec.and(fetchEmployees());
        if (selectionSet.contains("departments"))
            spec = spec.and(fetchDepartments());
        return repository.findOne(spec).orElseThrow();
    }

    @QueryMapping
    public Iterable<Organization> orgByName(@Argument String name){
        return repository.findOrganizationByNameContainingIgnoreCase(name);
    }

    private Specification<Organization> fetchDepartments() {
        return (root, query, builder) -> {
            Fetch<Organization, Department> f = root
                    .fetch("departments", JoinType.LEFT);
            Join<Organization, Department> join = (Join<Organization, Department>) f;
            return join.getOn();
        };
    }

    private Specification<Organization> fetchEmployees() {
        return (root, query, builder) -> {
            Fetch<Organization, Employee> f = root
                    .fetch("employees", JoinType.LEFT);
            Join<Organization, Employee> join = (Join<Organization, Employee>) f;
            return join.getOn();
        };
    }

    private Specification<Organization> byId(Integer id) {
        return (root, query, builder) -> builder.equal(root.get("id"), id);
    }
}
