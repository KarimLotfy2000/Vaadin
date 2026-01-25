package com.vaadin.vaadin_first_project.data.Univer.dto;

public record RowOp(
        String sheetId,
        RowOpType type,
        int rowIndex,
        int count
) {}