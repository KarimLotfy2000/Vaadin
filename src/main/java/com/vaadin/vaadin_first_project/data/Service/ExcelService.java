package com.vaadin.vaadin_first_project.data.Service;

 import com.vaadin.vaadin_first_project.data.Entity.ExcelDocument;
import com.vaadin.vaadin_first_project.data.Univer.utils.PoiToUniverStyleMapper;
 import com.vaadin.vaadin_first_project.data.Univer.utils.UniverToPoiStyleMapper;
 import com.vaadin.vaadin_first_project.data.Univer.dto.*;
import com.vaadin.vaadin_first_project.data.Univer.dto.styles.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;
 import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class ExcelService {

    private final ExcelDocumentService excelDocumentService;

    public ExcelService(ExcelDocumentService excelDocumentService) {
        this.excelDocumentService = excelDocumentService;
    }

     //------------------Convert Excel document to Univer format ------------------
    public UniverWorkbookData toUniverWorkbook(Long id){
        ExcelDocument document = excelDocumentService.getExcelDocument(id);
        byte[] data = document.getData();

        try(Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(data))) {
            Sheet sheet = wb.getSheetAt(0);

            String sheetId =  "1";
            String workbookId = "wb" + UUID.randomUUID().toString().replace("-", "");

            UsedRange usedRange = detectUsedRange(sheet);
            Map<Integer, Map<Integer, UniverCell>> cellData = new HashMap<>(); // rowIndex -> (colIndex -> UniverCell)

            // Merged regions
            List<UniverRange> mergeData = new ArrayList<>();
            for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                CellRangeAddress r = sheet.getMergedRegion(i);
                mergeData.add(new UniverRange(
                        r.getFirstRow(),
                        r.getFirstColumn(),
                        r.getLastRow(),
                        r.getLastColumn()
                ));
            }
            // Cell data
            for (int r = 0; r<= usedRange.lastRow; r++){
                Row row = sheet.getRow(r);
                if(row == null) continue;
                Map<Integer,UniverCell> rowMap = null; // colIndex -> UniverCell

                for (int c = 0; c<= usedRange.lastCol;c++){
                    Cell cell = row.getCell(c);

                    if(cell == null) continue;

                    UniverCell u = convertPoiCellToUniverCell(wb, cell);

                    if (u == null) continue;

                    if (rowMap == null) rowMap = new HashMap<>();

                    if (rowMap == null) rowMap = new HashMap<>();
                    rowMap.put(c, u);
                }
                if (rowMap != null) cellData.put(r,rowMap);


                }
            // Ensure minimum size
            int minRows = 100;   // to ensure height of Univer sheet
            int minCols = 26;
            int rowCount = Math.max(usedRange.lastRow + 1, minRows);
            int colCount = Math.max(usedRange.lastCol + 1, minCols);
             // Create UniverWorksheetData
            UniverWorksheetData worksheetData = new UniverWorksheetData(
                    sheetId,
                    sheet.getSheetName(),
                    rowCount,
                    colCount,
                    mergeData,
                    cellData
            );
            // Create sheets map
            Map<String, UniverWorksheetData> sheets = Map.of(sheetId, worksheetData);
            // Create and return UniverWorkbookData
            return new UniverWorkbookData(
                    workbookId,
                    document.getFilename(),
                    "1.0.0",
                    "en-US",
                    List.of(sheetId),
                    Collections.emptyMap(), // styles can be added later
                    sheets
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to convert Excel document to Univer format", e);
        }
    }

    // Convert a POI Cell to a UniverCell
    private UniverCell convertPoiCellToUniverCell(Workbook wb, Cell cell) {
        CellStyle cs = cell.getCellStyle();
        UniverStyleData style = PoiToUniverStyleMapper.mapStyle(wb, cs);

        Object v = null;
        Integer t = null;

        switch (cell.getCellType()) {
            case STRING -> { v = cell.getStringCellValue(); t = 1; }
            case BOOLEAN -> { v = cell.getBooleanCellValue() ? 1 : 0; t = 3; }
            case NUMERIC -> { v = cell.getNumericCellValue(); t = 2; }
            case BLANK -> {}
            default -> {
                 String s = new DataFormatter().formatCellValue(cell);
                if (s != null && !s.isBlank()) { v = s; t = 1; }
            }
        }
        boolean hasValue =
                v != null && (!(v instanceof String sv) || !sv.isBlank());

        boolean hasStyle = PoiToUniverStyleMapper.hasRenderableStyle(style);
        if (!hasValue && !hasStyle) return null;

        // UniverCell must allow v null but s not null
        return new UniverCell(v, style, t);
    }


    //------------------Apply snapshot values to Excel document ------------------

    public byte[] applySnapShot(Long documentId, UniverWorkbookData snapshot) {
        byte[] base = excelDocumentService.getExcelDocument(documentId).getData();

        try (InputStream is = new ByteArrayInputStream(base);
             Workbook wb = WorkbookFactory.create(is);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            UniverToPoiStyleMapper.Context styleCtx = new UniverToPoiStyleMapper.Context(wb);

            List<String> order = snapshot.sheetOrder();
            if (order == null || order.isEmpty()) {
                throw new IllegalArgumentException("Snapshot sheetOrder is empty");
            }


            for (int i = 0; i < order.size(); i++) {
            String sheetId = order.get(i);
            UniverWorksheetData ws = snapshot.sheets().get(sheetId);
            if (ws == null) continue;

            Sheet poiSheet = resolvePoiSheet(wb, i, ws);
                applyMerges(poiSheet, ws.mergeData());
                applyWorksheetValuesAndStyles(poiSheet, ws, snapshot, styleCtx);
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            wb.write(bos);
            return bos.toByteArray();
        }

    } catch (Exception e) {
        throw new RuntimeException("applySnapshotValues failed", e);
    }
 }

    private void applyMerges(Sheet sheet, List<UniverRange> merges) {
        //firsr remove existing merges to avoid duplicates
        for (int i = sheet.getNumMergedRegions() - 1; i >= 0; i--) {
            sheet.removeMergedRegion(i);
        }
        if (merges == null) return;

        for (UniverRange r : merges) {
            sheet.addMergedRegion(new CellRangeAddress(
                    r.startRow(), r.endRow(),
                    r.startColumn(), r.endColumn()
            ));
        }
    }

    private void applyWorksheetValuesAndStyles(Sheet sheet, UniverWorksheetData ws,UniverWorkbookData snapshot,UniverToPoiStyleMapper.Context styleCtx) {
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
                UniverToPoiStyleMapper.applyCellStyle(
                        styleCtx,
                        cell,
                        snapshot,
                        uCell
                );
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









