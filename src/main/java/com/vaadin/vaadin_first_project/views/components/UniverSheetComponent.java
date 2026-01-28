package com.vaadin.vaadin_first_project.views.components;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.vaadin_first_project.data.Service.ExcelDocumentService;
import com.vaadin.vaadin_first_project.data.Service.ExcelService;
import com.vaadin.vaadin_first_project.data.Univer.dto.UniverWorkbookData;

@Tag("univer-sheet")
@JsModule("./src/univer-sheet.ts")
public class UniverSheetComponent extends VerticalLayout {

    private final ExcelService excelService;
    private final ExcelDocumentService documentService;

    private static final ObjectMapper OM = new ObjectMapper();
    private Long documentId;

    public UniverSheetComponent(ExcelService excelService, ExcelDocumentService documentService) {
        this.excelService = excelService;
        this.documentService = documentService;
        setWidthFull();
        getElement().getStyle().set("display", "block");
    }
    public void render(UniverWorkbookData workbookData) {
        try {
            String json = OM.writeValueAsString(workbookData);
            getElement().callJsFunction("renderWorkbookJson", json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize workbook data", e);
        }
    }

    public void requestSnapShot(Long documentId){
        getElement().callJsFunction("requestSnapshot");
        this.documentId = documentId;

    }


    @ClientCallable
    public void saveSnapshot (String snapshotJson){
        if (documentId == null) throw new IllegalStateException("documentId not set");

        try {
            UniverWorkbookData snapshot = OM.readValue(snapshotJson, UniverWorkbookData.class);
            byte[] updated = excelService.applySnapshotValues(documentId, snapshot);
            documentService.saveAsCopy(documentId, updated);
        } catch (Exception e) {
            throw new RuntimeException("saveSnapshot failed", e);
        }
    }

    }

