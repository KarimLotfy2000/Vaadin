package com.vaadin.vaadin_first_project.data.Univer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UniverWorksheetData(
        String id,
        String name,
        Integer rowCount,
        Integer columnCount,
        Map<Integer, Map<Integer, UniverCell>> cellData
) {}