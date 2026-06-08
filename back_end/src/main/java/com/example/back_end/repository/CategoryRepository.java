package com.example.back_end.repository;

import com.example.back_end.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c WHERE c.deleted = 0 AND (c.bookId IS NULL OR c.bookId = :bookId)")
    List<Category> findByBookIdOrSystem(Long bookId);
}
