package com.vaadin.vaadin_first_project.data.Repository;

import com.vaadin.vaadin_first_project.data.Entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {
    @Query("SELECT c FROM Contact c WHERE " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :filterText, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :filterText, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :filterText, '%'))")
    List<Contact> search(@Param("filterText") String filterText);
}
