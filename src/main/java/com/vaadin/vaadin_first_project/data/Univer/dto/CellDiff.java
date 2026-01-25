package com.vaadin.vaadin_first_project.data.Univer.dto;


public record CellDiff(
        String sheetId,
        int r,
        int c,
        Object v
) {}