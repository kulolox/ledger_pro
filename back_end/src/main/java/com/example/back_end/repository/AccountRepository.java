package com.example.back_end.repository;

import com.example.back_end.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByBookIdAndDeleted(Long bookId, Integer deleted);
}
