package com.vaadin.vaadin_first_project.data.Service;

import com.vaadin.vaadin_first_project.data.Entity.Company;
import com.vaadin.vaadin_first_project.data.Entity.Contact;
import com.vaadin.vaadin_first_project.data.Entity.Status;
import com.vaadin.vaadin_first_project.data.Repository.CompanyRepository;
import com.vaadin.vaadin_first_project.data.Repository.ContactRepository;
import com.vaadin.vaadin_first_project.data.Repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrmService {
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private StatusRepository statusRepository;

  // Contacts
    public List<Contact> findAllContacts(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            return contactRepository.findAll();
        } else {
            return contactRepository.search(filterText);
        }
    }

    public long countContacts() {
        return contactRepository.count();
    }

    public void deleteContact(Contact contact) {
        contactRepository.delete(contact);
    }

    public void saveContact(Contact contact) {
        if (contact == null) {
            System.err.println("Contact is null. Are you sure you have connected your form to the application?");
            return;
        }
        contactRepository.save(contact);
    }
    // Companies
    public List<Company> findAllCompanies() {
        return companyRepository.findAll();
    }

    //Statuses
    public List<Status> findAllStatuses() {
        return statusRepository.findAll();
    }

}
