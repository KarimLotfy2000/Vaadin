package com.vaadin.vaadin_first_project.data.Univer.dto.styles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UniverRange(
        int startRow,
        int startColumn,
        int endRow,
        int endColumn
) {}