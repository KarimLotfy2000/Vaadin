package com.vaadin.vaadin_first_project.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.vaadin_first_project.data.Entity.ExcelDocument;
import com.vaadin.vaadin_first_project.data.Service.ExcelDocumentService;
import com.vaadin.vaadin_first_project.data.Service.ExcelService;
import com.vaadin.vaadin_first_project.data.Univer.dto.UniverWorkbookData;
import com.vaadin.vaadin_first_project.views.components.UniverSheetComponent;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Route(value = "excel-editor", layout = MainLayout.class)
public class ExcelView extends VerticalLayout {

    private final ExcelDocumentService docService;
    private final ExcelService excelService;

    private Long selectedDocId;

    private final Span uploadInfo = new Span("Noch keine Datei hochgeladen.");
    private final Grid<ExcelDocument> docsGrid = new Grid<>(ExcelDocument.class, false);

    private final UniverSheetComponent univer = new UniverSheetComponent();
    private final VerticalLayout editorWrapper = new VerticalLayout();

    private Upload upload;
    private Button renderBtn;
    private Button refreshBtn;
    private Button downloadBtn;

    public ExcelView(ExcelDocumentService docService, ExcelService excelService) {
        this.docService = docService;
        this.excelService = excelService;

        upload = buildUpload();
        renderBtn = buildRenderButton();
        refreshBtn = new Button("Liste aktualisieren", e -> refreshGrid());
        downloadBtn = new Button("Datei herunterladen");
        downloadBtn.setVisible(false);

        configureRootLayout();
        configureGrid();

        configureEditorArea();          // configure + add children ONCE
        editorWrapper.setVisible(false); // hide container initially

        add(
                new H3("Vordruck Editor (Upload -> DB BLOB -> Univer Render)"),
                upload,
                uploadInfo,
                new HorizontalLayout(refreshBtn, renderBtn),
                docsGrid,
                editorWrapper
        );

        refreshGrid();
    }

    private void configureRootLayout() {
        setWidthFull();
        setPadding(true);
        setSpacing(true);
    }

    private void configureEditorArea() {
        editorWrapper.setWidthFull();
        editorWrapper.setSpacing(true);
        editorWrapper.setPadding(true);
        editorWrapper.getStyle().set("padding", "var(--lumo-space-xl)");

        editorWrapper.removeAll();
        editorWrapper.add(univer, downloadBtn);
    }

    private void configureGrid() {
        docsGrid.addColumn(ExcelDocument::getId).setHeader("ID").setAutoWidth(true);
        docsGrid.addColumn(ExcelDocument::getFilename).setHeader("Datei").setFlexGrow(1);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());

        docsGrid.addColumn(d -> d.getCreatedAt() == null ? "" : fmt.format(d.getCreatedAt()))
                .setHeader("Upload-Zeit")
                .setAutoWidth(true);

        docsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        docsGrid.asSingleSelect().addValueChangeListener(e -> {
            ExcelDocument item = e.getValue();
            selectedDocId = (item != null) ? item.getId() : null;
        });

        docsGrid.setAllRowsVisible(true);
        docsGrid.getStyle().set("overflow", "visible");
    }

    private Upload buildUpload() {
        InMemoryUploadHandler handler = UploadHandler.inMemory((metadata, data) -> {
            String fileName = metadata.fileName();
            String mimeType = metadata.contentType();

            Long newId = docService.saveExcelDocument(fileName, mimeType, data);
            uploadInfo.setText("Hochgeladen: " + fileName + " (ID: " + newId + ")");

            refreshGrid();
            selectInGridIfPresent(newId);
        });

        Upload upload = new Upload(handler);
        upload.setAcceptedFileTypes(".xlsx", ".xlsm", ".xls");
        upload.setMaxFiles(1);
        return upload;
    }

    private Button buildRenderButton() {
        return new Button("Im Editor anzeigen", click -> {
            if (selectedDocId == null) {
                Notification.show("Bitte eine Datei aus der Liste auswählen.");
                return;
            }
            UniverWorkbookData workbookData = excelService.toUniverWorkbook(selectedDocId);
            editorWrapper.setVisible(true);
            downloadBtn.setVisible(true);
            univer.render(workbookData);
            univer.scrollIntoView();
        });
    }

    private void refreshGrid() {
        docsGrid.setItems(docService.getAllExcelDocuments());
        selectedDocId = null;
        downloadBtn.setVisible(false);
    }

    private void selectInGridIfPresent(Long id) {
        docService.getAllExcelDocuments().stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .ifPresent(docsGrid.asSingleSelect()::setValue);
    }
}
