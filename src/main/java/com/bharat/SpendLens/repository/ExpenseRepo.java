package com.bharat.SpendLens.repository;

import com.bharat.SpendLens.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface ExpenseRepo extends JpaRepository<Expense,Long>{

    @Query("SELECT e FROM Expense e WHERE e.id = :id AND e.userId = :userId")
    Optional<Expense> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);


    @Query("SELECT e FROM Expense e WHERE e.userId = :userId " +
            "AND (:category IS NULL OR LOWER(e.category) = LOWER(:category)) " +
            "AND (:startDate IS NULL OR e.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR e.createdAt <= :endDate)")
    Page<Expense> findByUserIdAndFilters(Long userId, String category, Instant startDate, Instant endDate, PageRequest of);
}
