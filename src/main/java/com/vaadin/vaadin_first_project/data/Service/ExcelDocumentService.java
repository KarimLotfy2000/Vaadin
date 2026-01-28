package com.vaadin.vaadin_first_project.data.Service;

import com.vaadin.vaadin_first_project.data.Entity.ExcelDocument;
import com.vaadin.vaadin_first_project.data.Repository.ExcelDocumentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class ExcelDocumentService {

    private final ExcelDocumentRepository repository;

    public ExcelDocumentService(ExcelDocumentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Long saveExcelDocument(String fileName,String contentType, byte[] data) {
        ExcelDocument document = new ExcelDocument();
        document.setFilename(fileName);
        document.setContentType(contentType);
        document.setData(data);
        ExcelDocument savedDocument = repository.save(document);
        return savedDocument.getId();

    }

    @Transactional
    public List<ExcelDocument> getAllExcelDocuments() {
        return repository.findAll();
    }



    @Transactional
    public ExcelDocument getExcelDocument(Long id) {
        return repository.findById(id).orElseThrow(()->new IllegalArgumentException("Excel document not found with id: " + id));
    }


   @Transactional
    public void saveAsCopy(Long documentId, byte[] updated) {
        ExcelDocument original = getExcelDocument(documentId);
        ExcelDocument copy = new ExcelDocument();
        copy.setFilename("Copy_"+ original.getFilename());
        copy.setContentType(original.getContentType());
        copy.setData(updated);
        repository.save(copy);

    }
}
