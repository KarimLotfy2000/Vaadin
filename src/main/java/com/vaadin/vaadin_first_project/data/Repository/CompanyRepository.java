package com.vaadin.vaadin_first_project.data.Repository;

import com.vaadin.vaadin_first_project.data.Entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Integer> {
}
