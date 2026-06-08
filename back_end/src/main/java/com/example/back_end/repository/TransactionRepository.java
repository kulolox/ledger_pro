package com.example.back_end.repository;

import com.example.back_end.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t WHERE t.bookId = :bookId AND t.deleted = 0 " +
           "AND (:startTime IS NULL OR t.transTime >= :startTime) " +
           "AND (:endTime IS NULL OR t.transTime <= :endTime) " +
           "ORDER BY t.transTime DESC")
    Page<Transaction> findByBookIdAndTimeRange(Long bookId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.bookId = :bookId AND t.deleted = 0 " +
           "AND t.transTime >= :monthStart AND t.transTime < :monthEnd " +
           "ORDER BY t.transTime DESC")
    List<Transaction> findByBookIdAndMonth(Long bookId, LocalDateTime monthStart, LocalDateTime monthEnd);

    @Query("SELECT t.type, t.categoryId, SUM(t.amount), COUNT(t) FROM Transaction t " +
           "WHERE t.bookId = :bookId AND t.deleted = 0 " +
           "AND t.transTime >= :monthStart AND t.transTime < :monthEnd " +
           "GROUP BY t.type, t.categoryId ORDER BY t.type, SUM(t.amount) DESC")
    List<Object[]> aggregateByCategory(Long bookId, LocalDateTime monthStart, LocalDateTime monthEnd);
}
