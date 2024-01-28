package com.hillel.ua.graphql.repository;

import com.hillel.ua.graphql.entities.Organization;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrganizationRepository extends
        CrudRepository<Organization, Integer>, JpaSpecificationExecutor<Organization> {

    List<Organization> findOrganizationByNameContainingIgnoreCase(String name);
}

