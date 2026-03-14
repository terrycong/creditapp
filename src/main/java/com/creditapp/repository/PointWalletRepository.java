package com.creditapp.repository;

import com.creditapp.entity.Child;
import com.creditapp.entity.PointWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PointWalletRepository extends JpaRepository<PointWallet, Long> {

    /**
     * Find all point batches for a child that still have remaining points
     */
    @Query("SELECT pw FROM PointWallet pw " +
           "WHERE pw.child = :child " +
           "AND pw.remainingPoints > 0 " +
           "AND pw.fullySpent = false " +
           "AND pw.expired = false " +
           "ORDER BY pw.earnedDate ASC")
    List<PointWallet> findAvailableBatches(Child child);

    /**
     * Find all point batches for a child ordered by earned date (FIFO)
     */
    List<PointWallet> findByChildOrderByEarnedDateAsc(Child child);

    /**
     * Find expiring points within next N days
     */
    @Query("SELECT pw FROM PointWallet pw " +
           "WHERE pw.child = :child " +
           "AND pw.expirationDate IS NOT NULL " +
           "AND pw.expirationDate <= :maxDate " +
           "AND pw.remainingPoints > 0 " +
           "AND pw.expired = false " +
           "ORDER BY pw.expirationDate ASC")
    List<PointWallet> findExpiringPoints(@Param("child") Child child, 
                                         @Param("maxDate") LocalDate maxDate);

    /**
     * Find all expired points that haven't been marked as expired
     */
    @Query("SELECT pw FROM PointWallet pw " +
           "WHERE pw.expirationDate < :today " +
           "AND pw.remainingPoints > 0 " +
           "AND pw.expired = false")
    List<PointWallet> findAllExpiredPoints(@Param("today") LocalDate today);

    /**
     * Count total points by child
     */
    @Query("SELECT SUM(pw.remainingPoints) FROM PointWallet pw " +
           "WHERE pw.child = :child " +
           "AND pw.expired = false " +
           "AND pw.fullySpent = false")
    Integer getTotalPointsByChild(@Param("child") Child child);

    /**
     * Count points expiring soon
     */
    @Query("SELECT SUM(pw.remainingPoints) FROM PointWallet pw " +
           "WHERE pw.child = :child " +
           "AND pw.expirationDate <= :maxDate " +
           "AND pw.expirationDate >= :today " +
           "AND pw.expired = false")
    Integer getPointsExpiringByDate(@Param("child") Child child,
                                    @Param("today") LocalDate today,
                                    @Param("maxDate") LocalDate maxDate);

    /**
     * Count total expired points for a child
     */
    @Query("SELECT SUM(pw.originalPoints) FROM PointWallet pw " +
           "WHERE pw.child = :child " +
           "AND pw.expired = true")
    Integer getTotalExpiredPoints(@Param("child") Child child);
}
