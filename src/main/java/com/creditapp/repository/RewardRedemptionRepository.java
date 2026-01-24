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
}
