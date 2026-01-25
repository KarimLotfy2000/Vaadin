package com.vaadin.vaadin_first_project.data.Repository;

import com.vaadin.vaadin_first_project.data.Entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepository extends JpaRepository<Status, Integer> {
}
