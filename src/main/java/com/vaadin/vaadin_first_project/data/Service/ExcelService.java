package com.vaadin.vaadin_first_project.data.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helger.commons.codec.IByteArrayStreamDecoder;
import com.vaadin.vaadin_first_project.data.Entity.ExcelDocument;
import com.vaadin.vaadin_first_project.data.Univer.dto.CellDiff;
import com.vaadin.vaadin_first_project.data.Univer.dto.UniverCell;
import com.vaadin.vaadin_first_project.data.Univer.dto.UniverWorkbookData;
import com.vaadin.vaadin_first_project.data.Univer.dto.UniverWorksheetData;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
 import java.io.ByteArrayInputStream;
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

    public void validateDiffs(List<CellDiff> diffs){


    }

    public byte[] applyDiffs(List<CellDiff> diffs){
        return new byte[0];
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









