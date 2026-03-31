package com.creditapp.controller;

import com.creditapp.dto.*;
import com.creditapp.entity.User;
import com.creditapp.exception.BusinessException;
import com.creditapp.service.CouponService;
import com.creditapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon Management", description = "上网券管理 API (仅 PARENT 可用)")
public class CouponController {

    private final CouponService couponService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "创建上网券 (仅 PARENT)")
    public ResponseEntity<ApiResponse<CouponDTO>> createCoupon(
            @Valid @RequestBody CreateCouponRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User parent = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        CouponDTO coupon = couponService.createCoupon(request, parent.getId());
        return ResponseEntity.ok(ApiResponse.success(coupon));
    }

    @GetMapping
    @Operation(summary = "获取所有上网券 (仅 PARENT)")
    public ResponseEntity<ApiResponse<List<CouponDTO>>> getAllCoupons(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User parent = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        List<CouponDTO> coupons = couponService.getCouponsByParentId(parent.getId());
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 获取上网券 (仅 PARENT)")
    public ResponseEntity<ApiResponse<CouponDTO>> getCouponById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User parent = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        CouponDTO coupon = couponService.getCouponById(id);
        return ResponseEntity.ok(ApiResponse.success(coupon));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根据代码获取上网券 (仅 PARENT)")
    public ResponseEntity<ApiResponse<CouponDTO>> getCouponByCode(
            @PathVariable String code,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User parent = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        CouponDTO coupon = couponService.getCouponByCode(code);
        return ResponseEntity.ok(ApiResponse.success(coupon));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新上网券 (仅 PARENT)")
    public ResponseEntity<ApiResponse<CouponDTO>> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CreateCouponRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User parent = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        CouponDTO coupon = couponService.updateCoupon(id, request, parent.getId());
        return ResponseEntity.ok(ApiResponse.success(coupon));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除上网券 (仅 PARENT)")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User parent = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        couponService.deleteCoupon(id, parent.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索上网券 (仅 PARENT)")
    public ResponseEntity<ApiResponse<List<CouponDTO>>> searchCoupons(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String username,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User parent = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        List<CouponDTO> coupons = couponService.searchCoupons(code, enabled, username);
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @PostMapping("/import")
    @Operation(summary = "批量导入上网券 (仅 PARENT)")
    public ResponseEntity<ApiResponse<List<CouponDTO>>> importCoupons(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            User parent = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
            List<CreateCouponRequest> coupons = parseCouponFile(file);
            
            List<CouponDTO> createdCoupons = new ArrayList<>();
            for (CreateCouponRequest request : coupons) {
                try {
                    CouponDTO coupon = couponService.createCoupon(request, parent.getId());
                    createdCoupons.add(coupon);
                } catch (Exception e) {
                    // Skip failed coupons
                    System.err.println("Failed to import: " + request.getCode() + " - " + e.getMessage());
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(createdCoupons));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("IMPORT_FAILED", "Failed to import coupons: " + e.getMessage()));
        }
    }

    @PostMapping("/redeem")
    @Operation(summary = "兑换上网券 (CHILD 可用)")
    public ResponseEntity<ApiResponse<CouponDTO>> redeemCoupon(
            @RequestParam String code,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        CouponDTO coupon = couponService.redeemCoupon(code, user.getId());
        return ResponseEntity.ok(ApiResponse.success(coupon));
    }

    private List<CreateCouponRequest> parseCouponFile(MultipartFile file) throws Exception {
        List<CreateCouponRequest> coupons = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                CreateCouponRequest request = parseCouponLine(line);
                if (request != null) {
                    coupons.add(request);
                }
            }
        }
        
        return coupons;
    }

    private CreateCouponRequest parseCouponLine(String line) {
        // Format: id=2 enabled=yes comment= username=Y74GNZMKZ2 timeout=1800 used=0
        CreateCouponRequest request = new CreateCouponRequest();
        
        String[] parts = line.split("\\s+");
        String code = null;
        
        for (String part : parts) {
            if (part.startsWith("id=")) {
                // Use id as part of code if no explicit code
                String id = part.substring(3);
                if (code == null) {
                    code = "COUPON_" + id;
                }
            } else if (part.startsWith("enabled=")) {
                request.setEnabled(part.substring(8).equalsIgnoreCase("yes"));
            } else if (part.startsWith("comment=")) {
                String comment = part.substring(8);
                if (!comment.isEmpty()) {
                    request.setComment(comment);
                }
            } else if (part.startsWith("username=")) {
                request.setUsername(part.substring(9));
            } else if (part.startsWith("timeout=")) {
                request.setTimeoutSeconds(Integer.parseInt(part.substring(8)));
            }
        }
        
        // Generate code if not present
        if (code == null) {
            code = "COUPON_" + System.currentTimeMillis();
        }
        request.setCode(code);
        
        // Default points to 100 if not specified
        if (request.getPoints() == null) {
            request.setPoints(100);
        }
        
        return request;
    }
}
