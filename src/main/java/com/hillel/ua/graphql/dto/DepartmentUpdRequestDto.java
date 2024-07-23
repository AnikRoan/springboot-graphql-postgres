package com.hillel.ua.graphql.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentUpdRequestDto {
    private String name;
    private Integer organizationId;
}

