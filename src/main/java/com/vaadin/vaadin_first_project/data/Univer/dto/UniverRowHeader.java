package com.vaadin.vaadin_first_project.data.Univer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UniverRowHeader(Integer width, Integer hidden) {} // hidden 0/1
