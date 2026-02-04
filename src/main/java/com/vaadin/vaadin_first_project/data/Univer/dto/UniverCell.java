package com.vaadin.vaadin_first_project.data.Univer.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.vaadin.vaadin_first_project.data.Univer.dto.styles.UniverStyleData;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UniverCell(
        Object v,
        Object s,     // String styleId OR UniverStyleData object
        Integer t
) {}