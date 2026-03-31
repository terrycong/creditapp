package com.creditapp.service;

import com.creditapp.dto.CouponDTO;
import com.creditapp.dto.CreateCouponRequest;
import com.creditapp.entity.Coupon;
import com.creditapp.entity.User;
import com.creditapp.exception.ResourceNotFoundException;
import com.creditapp.exception.BusinessException;
import com.creditapp.repository.CouponRepository;
import com.creditapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    public CouponDTO toDTO(Coupon coupon) {
        return CouponDTO.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .points(coupon.getPoints())
                .enabled(coupon.getEnabled())
                .comment(coupon.getComment())
                .username(coupon.getUsername())
                .timeoutSeconds(coupon.getTimeoutSeconds())
                .usedCount(coupon.getUsedCount())
                .redeemed(coupon.getRedeemed())
                .redeemedById(coupon.getRedeemedBy() != null ? coupon.getRedeemedBy().getId() : null)
                .redeemedByUsername(coupon.getRedeemedBy() != null ? coupon.getRedeemedBy().getUsername() : null)
                .redeemedAt(coupon.getRedeemedAt())
                .createdById(coupon.getCreatedBy() != null ? coupon.getCreatedBy().getId() : null)
                .createdByUsername(coupon.getCreatedBy() != null ? coupon.getCreatedBy().getUsername() : null)
                .insertedAt(coupon.getInsertedAt())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }

    public Coupon toEntity(CreateCouponRequest request) {
        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode());
        coupon.setPoints(request.getPoints());
        coupon.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        coupon.setComment(request.getComment());
        coupon.setUsername(request.getUsername());
        coupon.setTimeoutSeconds(request.getTimeoutSeconds() != null ? request.getTimeoutSeconds() : 600);
        coupon.setUsedCount(0);
        coupon.setInsertedAt(LocalDateTime.now());
        
        return coupon;
    }

    @Transactional
    public CouponDTO createCoupon(CreateCouponRequest request, Long parentId) {
        // Verify parent exists and has PARENT role
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
        
        if (parent.getRole() != com.creditapp.entity.UserRole.PARENT) {
            throw new BusinessException("PERMISSION_DENIED", "Only PARENT users can create coupons");
        }

        // Check if code already exists
        if (couponRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists: " + request.getCode());
        }

        Coupon coupon = toEntity(request);
        coupon.setCreatedBy(parent);
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());

        Coupon saved = couponRepository.save(coupon);
        return toDTO(saved);
    }

    @Transactional
    public CouponDTO updateCoupon(Long id, CreateCouponRequest request, Long parentId) {
        // Verify parent exists and has PARENT role
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
        
        if (parent.getRole() != com.creditapp.entity.UserRole.PARENT) {
            throw new BusinessException("PERMISSION_DENIED", "Only PARENT users can update coupons");
        }

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        // Update fields
        if (request.getCode() != null) {
            if (!coupon.getCode().equals(request.getCode()) && couponRepository.existsByCode(request.getCode())) {
                throw new IllegalArgumentException("Coupon code already exists: " + request.getCode());
            }
            coupon.setCode(request.getCode());
        }
        if (request.getPoints() != null) {
            coupon.setPoints(request.getPoints());
        }
        if (request.getEnabled() != null) {
            coupon.setEnabled(request.getEnabled());
        }
        if (request.getComment() != null) {
            coupon.setComment(request.getComment());
        }
        if (request.getUsername() != null) {
            coupon.setUsername(request.getUsername());
        }
        if (request.getTimeoutSeconds() != null) {
            coupon.setTimeoutSeconds(request.getTimeoutSeconds());
        }

        coupon.setUpdatedAt(LocalDateTime.now());
        Coupon updated = couponRepository.save(coupon);
        return toDTO(updated);
    }

    @Transactional
    public void deleteCoupon(Long id, Long parentId) {
        // Verify parent exists and has PARENT role
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
        
        if (parent.getRole() != com.creditapp.entity.UserRole.PARENT) {
            throw new BusinessException("PERMISSION_DENIED", "Only PARENT users can delete coupons");
        }

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        
        couponRepository.delete(coupon);
    }

    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CouponDTO getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        return toDTO(coupon);
    }

    public CouponDTO getCouponByCode(String code) {
        List<Coupon> coupons = couponRepository.findByCode(code);
        if (coupons.isEmpty()) {
            throw new ResourceNotFoundException("Coupon not found: " + code);
        }
        // Return first coupon with this code
        return toDTO(coupons.get(0));
    }

    public List<CouponDTO> getCouponsByParentId(Long parentId) {
        return couponRepository.findByCreatedBy_Id(parentId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CouponDTO> searchCoupons(String code, Boolean enabled, String username) {
        return couponRepository.searchCoupons(code, enabled, username).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CouponDTO redeemCoupon(String code, Long childId) {
        // Find an available coupon with this code (not redeemed, enabled, not expired)
        List<Coupon> coupons = couponRepository.findByCode(code);
        
        if (coupons.isEmpty()) {
            throw new ResourceNotFoundException("Invalid coupon code: " + code);
        }
        
        // Find first available coupon
        Coupon coupon = coupons.stream()
                .filter(c -> c.getEnabled() && !c.getRedeemed())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No available coupons with this code (all redeemed)"));

        // Check if assigned to specific user
        if (coupon.getUsername() != null && !coupon.getUsername().isEmpty()) {
            User child = userRepository.findById(childId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            if (!child.getUsername().equals(coupon.getUsername())) {
                throw new IllegalArgumentException("This coupon is not assigned to you");
            }
        }

        // Mark as redeemed
        coupon.setRedeemed(true);
        coupon.setRedeemedBy(userRepository.findById(childId).orElse(null));
        coupon.setRedeemedAt(LocalDateTime.now());
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        coupon.setUpdatedAt(LocalDateTime.now());
        
        Coupon updated = couponRepository.save(coupon);
        
        return toDTO(updated);
    }

    @Transactional
    public void importCoupons(List<CreateCouponRequest> coupons, Long parentId) {
        for (CreateCouponRequest request : coupons) {
            try {
                createCoupon(request, parentId);
            } catch (Exception e) {
                // Log error but continue with other coupons
                System.err.println("Failed to import coupon: " + request.getCode() + " - " + e.getMessage());
            }
        }
    }
}
