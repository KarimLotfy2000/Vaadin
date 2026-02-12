package com.vaadin.vaadin_first_project.data.Univer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UniverColumnData(Integer w, Integer hd) {} // width px, hidden 0/1
