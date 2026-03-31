package com.creditapp.repository;

import com.creditapp.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    List<Coupon> findByCode(String code);
    
    List<Coupon> findByEnabled(Boolean enabled);
    
    List<Coupon> findByCreatedBy_Id(Long userId);
    
    @Query("SELECT c FROM Coupon c WHERE (:code IS NULL OR c.code = :code) " +
           "AND (:enabled IS NULL OR c.enabled = :enabled) " +
           "AND (:username IS NULL OR c.username = :username)")
    List<Coupon> searchCoupons(@Param("code") String code, 
                               @Param("enabled") Boolean enabled,
                               @Param("username") String username);
    
    boolean existsByCode(String code);
}
