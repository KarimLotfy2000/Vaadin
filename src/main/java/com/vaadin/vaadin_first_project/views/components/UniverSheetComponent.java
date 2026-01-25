package com.vaadin.vaadin_first_project.views.components;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.vaadin_first_project.data.Univer.dto.UniverWorkbookData;

@Tag("univer-sheet")
@JsModule("./src/univer-sheet.ts")
public class UniverSheetComponent extends VerticalLayout {

    private static final ObjectMapper OM = new ObjectMapper();

    public UniverSheetComponent() {
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
}
