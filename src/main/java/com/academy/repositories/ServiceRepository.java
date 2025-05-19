package com.academy.repositories;

import com.academy.models.Service;
import com.academy.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findAllByTagsContaining(Tag tag);
}
