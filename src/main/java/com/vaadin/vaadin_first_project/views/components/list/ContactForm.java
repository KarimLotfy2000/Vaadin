// java
package com.vaadin.vaadin_first_project.views.components.list;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.shared.Registration;
import com.vaadin.vaadin_first_project.data.Entity.Company;
import com.vaadin.vaadin_first_project.data.Entity.Contact;
import com.vaadin.vaadin_first_project.data.Entity.Status;

import java.util.List;

public class ContactForm extends FormLayout {

    private Contact contact;
    Binder<Contact> binder = new Binder<>(Contact.class);

    TextField firstName = new TextField("First name");
    TextField lastName = new TextField("Last name");
    TextField email = new TextField("Email");
    ComboBox<Company> company = new ComboBox<>("Company");
    ComboBox<Status> status = new ComboBox<>("Status");

    public Button save = new Button("Save");
    public Button delete = new Button("Delete");
    public Button cancel = new Button("Close");

    public ContactForm(List<Company> companies, List<Status> statuses) {

        firstName.setPlaceholder("Enter first name");
        lastName.setPlaceholder("Enter last name");
        email.setPlaceholder("Enter email address");

        company.setItems(companies);
        company.setItemLabelGenerator(Company::getName);
        company.setAllowCustomValue(false);
        company.setPlaceholder("Select a company");

        status.setItems(statuses);
        status.setItemLabelGenerator(Status::getName);
        status.setAllowCustomValue(false);
        status.setPlaceholder("Select status");

        // Explicit bindings with validators
        binder.forField(firstName)
                .asRequired("First name is required")
                .bind(Contact::getFirstName, Contact::setFirstName);

        binder.forField(lastName)
                .asRequired("Last name is required")
                .bind(Contact::getLastName, Contact::setLastName);

        binder.forField(email)
                .asRequired("Email is required")
                .withValidator(new EmailValidator("Enter a valid email address"))
                .bind(Contact::getEmail, Contact::setEmail);

        binder.forField(company)
                .asRequired("Company is required")
                .bind(Contact::getCompany, Contact::setCompany);

        binder.forField(status)
                .asRequired("Status is required")
                .bind(Contact::getStatus, Contact::setStatus);

        // update save button enabled state on validation changes
        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid() && contact != null));

        add(
                firstName,
                lastName,
                email,
                company,
                status,
                createButtonLayout()
        );

    }

    // Create button layout with styled buttons and keyboard shortcuts
    private Component createButtonLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, contact)));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        HorizontalLayout buttonLayout = new HorizontalLayout(save, delete, cancel);
        buttonLayout.setSpacing(true);
        buttonLayout.getStyle().set("margin-top", "20px");

        return buttonLayout;
    }

     public void setContact(Contact contact){
        this.contact = contact;

        if (contact == null) {
            binder.setBean(null);
            firstName.clear();
            lastName.clear();
            email.clear();
            company.clear();
            status.clear();
            save.setEnabled(false);
            delete.setEnabled(false);
        } else {
            binder.setBean(contact);
            save.setEnabled(binder.isValid());
            delete.setEnabled(contact.getId() != null);
        }
    }

    // Check if the form data is valid and fire SaveEvent if so
    public void validateAndSave(){
        if (contact == null) {
            return;
        }
        if (binder.validate().isOk()) {
            fireEvent(new SaveEvent(this, contact));
        }
    }

    // --- Events ---
    public static abstract class ContactFormEvent extends ComponentEvent<ContactForm> {
        private final Contact contact;

        protected ContactFormEvent(ContactForm source, Contact contact) {
            super(source, false);
            this.contact = contact;
        }
        public Contact getContact() { return contact; }
    }

    public static class SaveEvent extends ContactFormEvent {
        public SaveEvent(ContactForm source, Contact contact) { super(source, contact); }
    }
    public static class DeleteEvent extends ContactFormEvent {
        public DeleteEvent(ContactForm source, Contact contact) { super(source, contact); }
    }
    public static class CloseEvent extends ContactFormEvent {
        public CloseEvent(ContactForm source) { super(source, null); }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}