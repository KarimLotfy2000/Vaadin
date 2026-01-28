package com.vaadin.vaadin_first_project.data.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.vaadin_first_project.data.Entity.ExcelDocument;
import com.vaadin.vaadin_first_project.data.Univer.dto.UniverCell;
import com.vaadin.vaadin_first_project.data.Univer.dto.UniverWorkbookData;
import com.vaadin.vaadin_first_project.data.Univer.dto.UniverWorksheetData;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
 import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ExcelService {

    private final ExcelDocumentService excelDocumentService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExcelService(ExcelDocumentService excelDocumentService) {
        this.excelDocumentService = excelDocumentService;
    }

    public UniverWorkbookData toUniverWorkbook(Long id){
        ExcelDocument document = excelDocumentService.getExcelDocument(id);
        byte[] data = document.getData();

        try(Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(data))) {
            Sheet sheet = wb.getSheetAt(0);

            String sheetId =  "1";
            String workbookId = "wb" + UUID.randomUUID().toString().replace("-", "");

            UsedRange usedRange = detectUsedRange(sheet);
            Map<Integer, Map<Integer, UniverCell>> cellData = new HashMap<>(); // rowIndex -> (colIndex -> UniverCell)
            DataFormatter dataFormatter = new DataFormatter();

            for (int r = 0; r<= usedRange.lastRow; r++){
                Row row = sheet.getRow(r);
                if(row == null) continue;
                Map<Integer,UniverCell> rowMap = null; // colIndex -> UniverCell

                for (int c = 0; c<= usedRange.lastCol;c++){
                    Cell cell = row.getCell(c);

                    if(cell == null) continue;

                    String cellValue = dataFormatter.formatCellValue(cell);

                    if (cellValue == null || cellValue.isBlank()) continue;
                    if (rowMap == null) rowMap = new HashMap<>();

                    rowMap.put(c,new UniverCell(cellValue));
                }
                if (rowMap != null) cellData.put(r,rowMap);


                }

            int minRows = 100;   // to ensure height of Univer sheet
            int minCols = 26;
            int rowCount = Math.max(usedRange.lastRow + 1, minRows);
            int colCount = Math.max(usedRange.lastCol + 1, minCols);
            UniverWorksheetData worksheetData = new UniverWorksheetData(
                    sheetId,
                    sheet.getSheetName(),
                    rowCount,
                    colCount,
                    cellData
            );

            Map<String, UniverWorksheetData> sheets = Map.of(sheetId, worksheetData);

            return new UniverWorkbookData(
                    workbookId,
                    document.getFilename(),
                    "1.0.0",
                    "en-US",
                    List.of(sheetId),
                    sheets
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to convert Excel document to Univer format", e);
        }
    }



    public byte[] applySnapshotValues(Long documentId, UniverWorkbookData snapshot) {
        byte[] base = excelDocumentService.getExcelDocument(documentId).getData();

        try (InputStream is = new ByteArrayInputStream(base);
             Workbook wb = WorkbookFactory.create(is);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            List<String> order = snapshot.sheetOrder();
            if (order == null || order.isEmpty()) {
                throw new IllegalArgumentException("Snapshot sheetOrder is empty");
            }
            
        for (int i = 0; i < order.size(); i++) {
            String sheetId = order.get(i);
            UniverWorksheetData ws = snapshot.sheets().get(sheetId);
            if (ws == null) continue;

            Sheet poiSheet = resolvePoiSheet(wb, i, ws);
            applyWorksheetValues(poiSheet, ws);
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            wb.write(bos);
            return bos.toByteArray();
        }

    } catch (Exception e) {
        throw new RuntimeException("applySnapshotValues failed", e);
    }
 }

    private void applyWorksheetValues(Sheet sheet, UniverWorksheetData ws) {
        int rowCount = ws.rowCount() == null ? 0 : ws.rowCount();
        int colCount = ws.columnCount() == null ? 0 : ws.columnCount();


        Map<Integer, Map<Integer, UniverCell>> cellData = ws.cellData();
        if (cellData == null) return;

        for (var rowEntry : cellData.entrySet()) { // rowIndex (key) -> (colIndex -> UniverCell) (value)
            int r = rowEntry.getKey();
            if (r < 0 || (rowCount > 0 && r >= rowCount)) continue;

            Row row = sheet.getRow(r);
            if (row == null) row = sheet.createRow(r);

            Map<Integer, UniverCell> cols = rowEntry.getValue();
            if (cols == null) continue;

            for (var colEntry : cols.entrySet()) { // colIndex (key) -> UniverCell (value)
                int c = colEntry.getKey();
                if (c < 0 || (colCount > 0 && c >= colCount)) continue;

                UniverCell uCell = colEntry.getValue();
                Object v = (uCell == null) ? null : uCell.v();

                Cell cell = row.getCell(c);
                if (cell == null) cell = row.createCell(c);

                setCellValue(cell, v);
            }
        }
    }
    private void setCellValue(Cell cell, Object v) {
        switch (v) {
            case null -> {
                cell.setBlank();
                return;
            }
            case Number n -> {
                cell.setCellValue(n.doubleValue());
                return;
            }
            case Boolean b -> {
                cell.setCellValue(b);
                return;
            }
             case String s when s.isBlank() -> {
                cell.setCellValue("");
                return;
            }
            default -> {
            }
        }
        cell.setCellValue(String.valueOf(v));
    }

    private Sheet resolvePoiSheet(Workbook wb, int i, UniverWorksheetData ws) {
        if (i < wb.getNumberOfSheets()) {
            return wb.getSheetAt(i);
        }
        if (ws.name() != null) {
            Sheet byName = wb.getSheet(ws.name());
            if (byName != null) return byName;
            return wb.createSheet(ws.name());
        }
        return wb.createSheet("Sheet" + (i + 1));
    }



    private static final record UsedRange(int lastRow, int lastCol) { }

    private UsedRange detectUsedRange(Sheet sheet) {
        int lastRow = sheet.getLastRowNum();
        int lastCol = 0;

        for (int r = 0; r <= lastRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            short lc = row.getLastCellNum(); // lastCellNum is 1-based, can be -1
            if (lc > 0) lastCol = Math.max(lastCol, lc - 1);
        }

        return new UsedRange(lastRow, lastCol);

    }
}









