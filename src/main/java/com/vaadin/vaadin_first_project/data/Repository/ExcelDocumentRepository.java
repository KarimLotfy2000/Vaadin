package com.vaadin.vaadin_first_project.data.Repository;

import com.vaadin.vaadin_first_project.data.Entity.ExcelDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExcelDocumentRepository extends JpaRepository<ExcelDocument, Long> {
 }
