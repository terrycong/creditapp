package com.creditapp.controller;

import com.creditapp.dto.FeedbackDTO;
import com.creditapp.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 反馈管理控制器
 */
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 孩子提交反馈
     * POST /api/feedback/submit
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('CHILD')")
    public ResponseEntity<FeedbackDTO> submitFeedback(@RequestBody FeedbackDTO.SubmitRequest request) {
        try {
            FeedbackDTO result = feedbackService.submitFeedback(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 孩子查看自己的反馈列表
     * GET /api/feedback/my
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CHILD')")
    public ResponseEntity<List<FeedbackDTO>> getMyFeedbacks() {
        try {
            List<FeedbackDTO> feedbacks = feedbackService.getMyFeedbacks();
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 家长查看所有反馈（分页）
     * GET /api/feedback?page=0&size=20&sortBy=createdAt
     */
    @GetMapping
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<Page<FeedbackDTO>> getAllFeedbacks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy) {
        try {
            Page<FeedbackDTO> feedbacks = feedbackService.getAllFeedbacks(page, size, sortBy);
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 家长查看反馈详情
     * GET /api/feedback/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<FeedbackDTO> getFeedbackById(@PathVariable Long id) {
        try {
            FeedbackDTO feedback = feedbackService.getFeedbackById(id);
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 家长回复反馈
     * PUT /api/feedback/{id}/response
     */
    @PutMapping("/{id}/response")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<FeedbackDTO> respondToFeedback(
            @PathVariable Long id,
            @RequestBody FeedbackDTO.ResponseRequest request) {
        try {
            FeedbackDTO result = feedbackService.respondToFeedback(id, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 家长更新反馈状态
     * PUT /api/feedback/{id}/status?status=ACCEPTED
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<FeedbackDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            FeedbackDTO result = feedbackService.updateStatus(id, status);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取反馈统计信息
     * GET /api/feedback/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<FeedbackDTO.StatsDTO> getStats() {
        try {
            FeedbackDTO.StatsDTO stats = feedbackService.getStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取待处理的反馈列表
     * GET /api/feedback/pending
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<FeedbackDTO>> getPendingFeedbacks() {
        try {
            List<FeedbackDTO> feedbacks = feedbackService.getPendingFeedbacks();
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
