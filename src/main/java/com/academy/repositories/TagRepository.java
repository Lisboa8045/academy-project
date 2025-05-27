package com.academy.repositories;

import com.academy.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String tag1);

    List<Tag> findAllByNameIn(List<String> tagNames);
}
