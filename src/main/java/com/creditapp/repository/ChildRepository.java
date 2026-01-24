package com.creditapp.repository;

import com.creditapp.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChildRepository extends JpaRepository<Child, Long> {
    List<Child> findByParentId(Long parentId);
    java.util.Optional<Child> findByUsername(String username);
}
