package com.example.back_end.repository;

import com.example.back_end.entity.AccountBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountBookRepository extends JpaRepository<AccountBook, Long> {
    Optional<AccountBook> findByIdAndDeleted(Long id, Integer deleted);
    List<AccountBook> findByOwnerIdAndDeleted(Long ownerId, Integer deleted);
}
