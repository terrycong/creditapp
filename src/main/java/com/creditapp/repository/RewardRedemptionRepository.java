package com.creditapp.repository;

import com.creditapp.entity.Child;
import com.creditapp.entity.RewardRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RewardRedemptionRepository extends JpaRepository<RewardRedemption, Long> {
    List<RewardRedemption> findByChildId(Long childId);

    @Query("SELECT rr FROM RewardRedemption rr JOIN FETCH rr.reward WHERE rr.child.id = :childId")
    List<RewardRedemption> findByChildIdWithReward(@org.springframework.data.repository.query.Param("childId") Long childId);
    
    // Find recent reward redemptions for parent's children
    @Query("SELECT rr FROM RewardRedemption rr " +
           "JOIN FETCH rr.child c " +
           "JOIN FETCH rr.reward r " +
           "WHERE c.parent.id = :parentId " +
           "ORDER BY rr.redeemedAt DESC")
    List<RewardRedemption> findRecentRedemptionsByParentId(@org.springframework.data.repository.query.Param("parentId") Long parentId);
    
    // Find recent reward redemptions with limit
    @Query("SELECT rr FROM RewardRedemption rr " +
           "JOIN FETCH rr.child c " +
           "JOIN FETCH rr.reward r " +
           "WHERE c.parent.id = :parentId " +
           "ORDER BY rr.redeemedAt DESC")
    List<RewardRedemption> findRecentRedemptionsByParentIdWithLimit(
            @org.springframework.data.repository.query.Param("parentId") Long parentId,
            org.springframework.data.domain.Pageable pageable);
    
    // Count reward redemptions by child for a parent
    @Query("SELECT c.id, c.username, COUNT(rr) as redemptionCount, SUM(r.pointsRequired) as totalPointsSpent " +
           "FROM RewardRedemption rr " +
           "JOIN rr.child c " +
           "JOIN rr.reward r " +
           "WHERE c.parent.id = :parentId " +
           "GROUP BY c.id, c.username")
    List<Object[]> countRedemptionsByChildForParent(@org.springframework.data.repository.query.Param("parentId") Long parentId);
    
    // Get reward redemption statistics
    @Query("SELECT r.name, COUNT(rr) as redemptionCount " +
           "FROM RewardRedemption rr " +
           "JOIN rr.reward r " +
           "JOIN rr.child c " +
           "WHERE c.parent.id = :parentId " +
           "GROUP BY r.id, r.name " +
           "ORDER BY redemptionCount DESC")
    List<Object[]> getRewardRedemptionStats(@org.springframework.data.repository.query.Param("parentId") Long parentId);
}
