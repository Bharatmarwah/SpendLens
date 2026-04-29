package com.bharat.SpendLens.repository;

import com.bharat.SpendLens.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepo extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e WHERE e.id = :id AND e.userId = :userId")
    Optional<Expense> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);


    @Query("SELECT e FROM Expense e WHERE e.userId = :userId " +
            "AND (:category IS NULL OR LOWER(e.category) = LOWER(:category)) " +
            "AND (:minAmount IS NULL OR e.amount >= :minAmount) " +
            "AND (:maxAmount IS NULL OR e.amount <= :maxAmount) " +
            "AND (:startDate IS NULL OR e.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR e.createdAt <= :endDate)" +
            "ORDER BY e.createdAt DESC")
    Page<Expense> findByUserIdAndFilters(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            PageRequest of
    );

    @Query("SELECT e FROM Expense e WHERE e.userId = :userId " +
            "AND (:expenseId IS NULL OR e.id = :expenseId) " +
            "AND (:category IS NULL OR LOWER(e.category) = LOWER(:category)) " +
            "AND (:minAmount IS NULL OR e.amount >= :minAmount) " +
            "AND (:maxAmount IS NULL OR e.amount <= :maxAmount) " +
            "AND (:startDate IS NULL OR e.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR e.createdAt <= :endDate) " +
            "ORDER BY e.createdAt DESC")
    List<Expense> findExpenseByFilter(
            @Param("userId") Long userId,
            @Param("expenseId") Long expenseId,
            @Param("category") String category,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    List<Expense> findAllByIdInAndUserId(List<Long> ids, Long userId);
}
