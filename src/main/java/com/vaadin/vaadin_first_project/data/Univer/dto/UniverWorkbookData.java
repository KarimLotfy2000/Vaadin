package com.vaadin.vaadin_first_project.data.Univer.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.vaadin.vaadin_first_project.data.Univer.dto.styles.UniverStyleData;

import java.util.List;
import java.util.Map;
@JsonIgnoreProperties(ignoreUnknown = true)

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UniverWorkbookData(
        String id,
        String name,
        String appVersion,
        String locale,
        List<String> sheetOrder,
        Map<String, UniverStyleData> styles,
        Map<String, UniverWorksheetData> sheets
) {}