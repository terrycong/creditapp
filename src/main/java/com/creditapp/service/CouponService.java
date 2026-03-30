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
                .expiresAt(coupon.getExpiresAt())
                .timeoutSeconds(coupon.getTimeoutSeconds())
                .usedCount(coupon.getUsedCount())
                .createdById(coupon.getCreatedBy() != null ? coupon.getCreatedBy().getId() : null)
                .createdByUsername(coupon.getCreatedBy() != null ? coupon.getCreatedBy().getUsername() : null)
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
        
        // If expires is 0, set to far future (year 2099)
        if (request.getExpires() != null && request.getExpires() > 0) {
            coupon.setExpiresAt(LocalDateTime.now().plusDays(request.getExpires()));
        } else {
            coupon.setExpiresAt(LocalDateTime.of(2099, 12, 31, 23, 59, 59));
        }
        
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
        if (request.getExpires() != null) {
            if (request.getExpires() > 0) {
                coupon.setExpiresAt(LocalDateTime.now().plusDays(request.getExpires()));
            } else {
                coupon.setExpiresAt(LocalDateTime.of(2099, 12, 31, 23, 59, 59));
            }
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
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + code));
        return toDTO(coupon);
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
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid coupon code"));

        if (!coupon.getEnabled()) {
            throw new IllegalArgumentException("Coupon is disabled");
        }

        // Check if expired
        if (coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Coupon has expired");
        }

        // Check if assigned to specific user
        if (coupon.getUsername() != null && !coupon.getUsername().isEmpty()) {
            User child = userRepository.findById(childId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            if (!child.getUsername().equals(coupon.getUsername())) {
                throw new IllegalArgumentException("This coupon is not assigned to you");
            }
        }

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
