package com.vaadin.vaadin_first_project.data.Univer.dto;



import java.util.List;

public record SavePayload(
        Long docId,
        String sheetId,
        List<RowOp> rowOps,
        List<CellDiff> cellDiffs
) {}
