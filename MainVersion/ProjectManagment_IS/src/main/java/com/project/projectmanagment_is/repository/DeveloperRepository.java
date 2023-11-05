package com.project.projectmanagment_is.repository;

import com.project.projectmanagment_is.model.Developer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {
}
