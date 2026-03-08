package com.creditapp.controller;

import com.creditapp.dto.*;
import com.creditapp.repository.ChildRepository;
import com.creditapp.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.creditapp.util.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
@Tag(name = "Reward Management", description = "礼物管理API")
public class RewardController {

    private final RewardService rewardService;
    private final ChildRepository childRepository;

    @PostMapping
    @Operation(summary = "创建礼物")
    public ResponseEntity<ApiResponse<RewardDTO>> createReward(@Valid @RequestBody CreateTaskRequest request) {
        RewardDTO reward = rewardService.createReward(request);
        return ResponseEntity.ok(ApiResponse.success(reward));
    }

    @GetMapping
    @Operation(summary = "获取所有礼物")
    public ResponseEntity<ApiResponse<List<RewardDTO>>> getAllRewards() {
        List<RewardDTO> rewards = rewardService.getAllRewards();
        return ResponseEntity.ok(ApiResponse.success(rewards));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取礼物")
    public ResponseEntity<ApiResponse<RewardDTO>> getRewardById(@PathVariable Long id) {
        RewardDTO reward = rewardService.getRewardById(id);
        return ResponseEntity.ok(ApiResponse.success(reward));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新礼物")
    public ResponseEntity<ApiResponse<RewardDTO>> updateReward(@PathVariable Long id,
                                                             @Valid @RequestBody CreateTaskRequest request) {
        RewardDTO reward = rewardService.updateReward(id, request);
        return ResponseEntity.ok(ApiResponse.success(reward));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除礼物")
    public ResponseEntity<ApiResponse<Void>> deleteReward(@PathVariable Long id) {
        rewardService.deleteReward(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/redeem")
    @Operation(summary = "兑换礼物")
    public ResponseEntity<ApiResponse<RewardRedemptionDTO>> redeemReward(@PathVariable Long id,
                                                                       @AuthenticationPrincipal UserDetails userDetails) {
        Long childId = SecurityUtils.getChildIdFromUsername(userDetails.getUsername(), childRepository);
        RewardRedemptionDTO redemption = rewardService.redeemReward(id, childId);
        return ResponseEntity.ok(ApiResponse.success(redemption));
    }

    @PostMapping("/redemptions/{redemptionId}/use")
    @Operation(summary = "标记礼物为已使用")
    public ResponseEntity<ApiResponse<RewardRedemptionDTO>> useReward(@PathVariable Long redemptionId,
                                                                     @AuthenticationPrincipal UserDetails userDetails) {
        Long childId = SecurityUtils.getChildIdFromUsername(userDetails.getUsername(), childRepository);
        RewardRedemptionDTO redemption = rewardService.useReward(redemptionId, childId);
        return ResponseEntity.ok(ApiResponse.success(redemption));
    }
}