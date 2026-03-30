package com.creditapp.service;

import com.creditapp.dto.CouponDTO;
import com.creditapp.dto.CreateCouponRequest;
import com.creditapp.entity.Coupon;
import com.creditapp.entity.User;
import com.creditapp.entity.UserRole;
import com.creditapp.exception.BusinessException;
import com.creditapp.exception.ResourceNotFoundException;
import com.creditapp.repository.CouponRepository;
import com.creditapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CouponService couponService;

    private User parentUser;
    private User childUser;
    private Coupon coupon;
    private CreateCouponRequest createRequest;

    @BeforeEach
    void setUp() {
        // Setup parent user
        parentUser = new User();
        parentUser.setId(1L);
        parentUser.setUsername("parent@test.com");
        parentUser.setRole(UserRole.PARENT);

        // Setup child user
        childUser = new User();
        childUser.setId(2L);
        childUser.setUsername("child@test.com");
        childUser.setRole(UserRole.CHILD);

        // Setup coupon
        coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCode("TEST_COUPON_001");
        coupon.setPoints(100);
        coupon.setEnabled(true);
        coupon.setUsername(null); // Available for anyone
        coupon.setExpiresAt(LocalDateTime.now().plusDays(30));
        coupon.setTimeoutSeconds(600);
        coupon.setUsedCount(0);
        coupon.setRedeemed(false);
        coupon.setCreatedBy(parentUser);
        coupon.setCreatedAt(LocalDateTime.now());

        // Setup create request
        createRequest = new CreateCouponRequest();
        createRequest.setCode("NEW_COUPON");
        createRequest.setPoints(50);
        createRequest.setEnabled(true);
        createRequest.setTimeoutSeconds(300);
        createRequest.setExpires(0); // Never expires
    }

    @Test
    void testCreateCoupon_Success() {
        // Arrange
        Coupon newCoupon = new Coupon();
        newCoupon.setId(2L);
        newCoupon.setCode("NEW_COUPON");
        newCoupon.setPoints(50);
        newCoupon.setEnabled(true);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(parentUser));
        when(couponRepository.existsByCode("NEW_COUPON")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(newCoupon);

        // Act
        CouponDTO result = couponService.createCoupon(createRequest, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("NEW_COUPON", result.getCode());
        assertEquals(50, result.getPoints());
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void testCreateCoupon_ParentNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            couponService.createCoupon(createRequest, 999L);
        });
    }

    @Test
    void testCreateCoupon_NonParentUser() {
        // Arrange - child user trying to create coupon
        when(userRepository.findById(2L)).thenReturn(Optional.of(childUser));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            couponService.createCoupon(createRequest, 2L);
        });
    }

    @Test
    void testCreateCoupon_DuplicateCode() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(parentUser));
        when(couponRepository.existsByCode("NEW_COUPON")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            couponService.createCoupon(createRequest, 1L);
        });
    }

    @Test
    void testRedeemCoupon_Success() {
        // Arrange
        List<Coupon> coupons = new ArrayList<>();
        coupons.add(coupon);
        
        when(couponRepository.findByCode("TEST_COUPON_001")).thenReturn(coupons);
        when(userRepository.findById(2L)).thenReturn(Optional.of(childUser));
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        // Act
        CouponDTO result = couponService.redeemCoupon("TEST_COUPON_001", 2L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getRedeemed());
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void testRedeemCoupon_AllRedeemed() {
        // Arrange - all coupons already redeemed
        Coupon redeemedCoupon = new Coupon();
        redeemedCoupon.setId(1L);
        redeemedCoupon.setCode("TEST_COUPON_001");
        redeemedCoupon.setRedeemed(true);
        
        List<Coupon> coupons = new ArrayList<>();
        coupons.add(redeemedCoupon);
        
        when(couponRepository.findByCode("TEST_COUPON_001")).thenReturn(coupons);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            couponService.redeemCoupon("TEST_COUPON_001", 2L);
        });
    }

    @Test
    void testRedeemCoupon_Expired() {
        // Arrange - expired coupon
        Coupon expiredCoupon = new Coupon();
        expiredCoupon.setId(1L);
        expiredCoupon.setCode("TEST_COUPON_001");
        expiredCoupon.setEnabled(true);
        expiredCoupon.setRedeemed(false);
        expiredCoupon.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired
        
        List<Coupon> coupons = new ArrayList<>();
        coupons.add(expiredCoupon);
        
        when(couponRepository.findByCode("TEST_COUPON_001")).thenReturn(coupons);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            couponService.redeemCoupon("TEST_COUPON_001", 2L);
        });
    }

    @Test
    void testRedeemCoupon_UsernameMismatch() {
        // Arrange - coupon assigned to specific user
        Coupon assignedCoupon = new Coupon();
        assignedCoupon.setId(1L);
        assignedCoupon.setCode("TEST_COUPON_001");
        assignedCoupon.setEnabled(true);
        assignedCoupon.setRedeemed(false);
        assignedCoupon.setUsername("other_child@test.com");
        assignedCoupon.setExpiresAt(LocalDateTime.now().plusDays(30));
        
        List<Coupon> coupons = new ArrayList<>();
        coupons.add(assignedCoupon);
        
        when(couponRepository.findByCode("TEST_COUPON_001")).thenReturn(coupons);
        when(userRepository.findById(2L)).thenReturn(Optional.of(childUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            couponService.redeemCoupon("TEST_COUPON_001", 2L);
        });
    }

    @Test
    void testGetCouponsByParentId() {
        // Arrange
        List<Coupon> coupons = new ArrayList<>();
        coupons.add(coupon);
        
        when(couponRepository.findByCreatedBy_Id(1L)).thenReturn(coupons);

        // Act
        List<CouponDTO> result = couponService.getCouponsByParentId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TEST_COUPON_001", result.get(0).getCode());
    }

    @Test
    void testUpdateCoupon_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(parentUser));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        CreateCouponRequest updateRequest = new CreateCouponRequest();
        updateRequest.setPoints(200);
        updateRequest.setEnabled(false);

        // Act
        CouponDTO result = couponService.updateCoupon(1L, updateRequest, 1L);

        // Assert
        assertNotNull(result);
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void testDeleteCoupon_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(parentUser));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        doNothing().when(couponRepository).delete(any(Coupon.class));

        // Act
        couponService.deleteCoupon(1L, 1L);

        // Assert
        verify(couponRepository, times(1)).delete(any(Coupon.class));
    }

    @Test
    void testSearchCoupons() {
        // Arrange
        List<Coupon> coupons = new ArrayList<>();
        coupons.add(coupon);
        
        when(couponRepository.searchCoupons("TEST", null, null)).thenReturn(coupons);

        // Act
        List<CouponDTO> result = couponService.searchCoupons("TEST", null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
