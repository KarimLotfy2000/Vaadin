package com.vaadin.vaadin_first_project.views.components.list;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.vaadin_first_project.data.Entity.Contact;

 import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.vaadin_first_project.data.Entity.Company;
import com.vaadin.vaadin_first_project.data.Entity.Status;
import com.vaadin.vaadin_first_project.data.Service.CrmService;

import java.util.List;

 import com.vaadin.flow.component.html.Span;
import com.vaadin.vaadin_first_project.views.MainLayout;


@Route(value= "list", layout = MainLayout.class)
@PageTitle("Contacts List")
public class ListView extends VerticalLayout {
     Grid<Contact> grid = new Grid<>(Contact.class);
    TextField filterText = new TextField();
    private final CrmService service;

     public ListView(CrmService crmService) {
        this.service = crmService;
        addClassName("list-view");
        setSizeFull();
        configureGrid();
        add(getToolbar(), grid);
        renderList();
        configureSelection();
     }




    //Helper methods


    // Render list method to fetch and display contacts based on filter
    private void renderList() {
         grid.setItems(service.findAllContacts(filterText.getValue()));
    }

    // Configure selection listener for the grid
    private void configureSelection() {
        grid.asSingleSelect().addValueChangeListener(e -> {
            Contact selected = e.getValue();
            if (selected != null) {
                openContactDialog(selected);
            }
        });
    }
    // Create toolbar with filter text field and add contact button
    private Component getToolbar() {
        filterText.setPlaceholder("Filter by name ...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> renderList());

        Button addContactButton = new Button("Add contact");
        addContactButton.addClassName("add-contact-button");
        addContactButton.getStyle().set("background-color", "#1E3A8A");
        addContactButton.getStyle().set("color", "white");
        addContactButton.addClickListener(e -> {
            openContactDialog(new Contact());
            grid.asSingleSelect().clear();
        });

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addContactButton);
        toolbar.addClassName("toolbar");

        return toolbar;
    }

    // Open contact dialog for adding a new contact
    private void openContactDialog(Contact contact) {
        List<Company> companies = service.findAllCompanies();
        List<Status> statuses = service.findAllStatuses();

        ContactForm contactForm = new ContactForm(companies, statuses);
        contactForm.setContact(contact);

        Dialog dialog = new Dialog(contactForm);
        dialog.setModal(true);
        dialog.setWidth("480px");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        registerDialogListeners(contactForm, dialog);

        dialog.open();
    }

    // Register dialog listeners for save, delete, and close events
    private void registerDialogListeners(ContactForm form, Dialog dialog) {
        form.addListener(ContactForm.SaveEvent.class, e -> handleSave(e, dialog));
        form.addListener(ContactForm.DeleteEvent.class, e -> handleDelete(e, dialog));
        form.addListener(ContactForm.CloseEvent.class, e -> dialog.close());
    }
   // Handle save event from the contact form
    private void handleSave(ContactForm.SaveEvent event, Dialog dialog) {
        try {
            service.saveContact(event.getContact());
            Notification.show("Contact saved successfully", 2000, Notification.Position.BOTTOM_END);
        } catch (Exception ex) {
            Notification.show("Error saving contact: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END);
        } finally {
            closeDialogAndRefresh(dialog);
        }
    }
    // Handle delete event from the contact form
    private void handleDelete(ContactForm.DeleteEvent event, Dialog dialog) {
        Contact c = event.getContact();

        if (c != null && c.getId() != null) {
            service.deleteContact(c);
        }

        closeDialogAndRefresh(dialog);
    }
   // Close dialog and refresh the contact list
    private void closeDialogAndRefresh(Dialog dialog) {
        dialog.close();
        grid.asSingleSelect().clear();
        renderList();
    }



    // Configure the grid to display contact information
    private void  configureGrid() {
         grid.addClassName("contact-grid");
         grid.setSizeFull();
         grid.setColumns("firstName", "lastName", "email");
         grid.addColumn(contact -> {
            Company company = contact.getCompany();
            return company != null ? company.getName() : "";
         }).setHeader("Company");
        addColoredStatusSpans();
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    // Add colored status spans to the grid based on contact status
    private void addColoredStatusSpans() {
        grid.addComponentColumn(contact -> {
           Span statusSpan = new Span(contact.getStatus() != null ? contact.getStatus().getName() : "");
           if (contact.getStatus() != null && contact.getStatus().getId() != null) {
               int id = Math.toIntExact(contact.getStatus().getId());
               switch (id) {
                   case 1: statusSpan.getStyle().set("color", "#6B7280"); break;
                   case 2: statusSpan.getStyle().set("color", "#2563EB"); break;
                   case 3: statusSpan.getStyle().set("color", "#F59E0B"); break;
                   case 4: statusSpan.getStyle().set("color", "#16A34A"); break;
                   case 5: statusSpan.getStyle().set("color", "#DC2626"); break;
                   default: break;
               }
           }
           return statusSpan;
       }).setHeader("Status");
    }
}
