package com.creditapp.service;

import com.creditapp.dto.FeedbackDTO;
import com.creditapp.entity.Child;
import com.creditapp.entity.Feedback;
import com.creditapp.entity.User;
import com.creditapp.repository.ChildRepository;
import com.creditapp.repository.FeedbackRepository;
import com.creditapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 反馈服务层
 */
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ChildRepository childRepository;
    private final UserRepository userRepository;

    /**
     * 孩子提交反馈
     */
    @Transactional
    public FeedbackDTO submitFeedback(FeedbackDTO.SubmitRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // 获取当前孩子
        Child child = childRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Child not found: " + username));

        // 创建反馈
        Feedback feedback = new Feedback();
        feedback.setChild(child);
        feedback.setCategory(Feedback.Category.valueOf(request.getCategory()));
        feedback.setTitle(request.getTitle());
        feedback.setDescription(request.getDescription());
        feedback.setStatus(Feedback.Status.PENDING);

        feedbackRepository.save(feedback);
        return FeedbackDTO.fromEntity(feedback);
    }

    /**
     * 孩子查看自己的反馈列表
     */
    @Transactional(readOnly = true)
    public List<FeedbackDTO> getMyFeedbacks() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Child child = childRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Child not found: " + username));

        List<Feedback> feedbacks = feedbackRepository.findByChildIdOrderByCreatedAtDesc(child.getId());
        return feedbacks.stream()
                .map(FeedbackDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 家长查看所有反馈（分页）
     */
    @Transactional(readOnly = true)
    public Page<FeedbackDTO> getAllFeedbacks(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy != null ? sortBy : "createdAt"));
        return feedbackRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(FeedbackDTO::fromEntity);
    }

    /**
     * 家长查看反馈详情
     */
    @Transactional(readOnly = true)
    public FeedbackDTO getFeedbackById(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found: " + id));
        return FeedbackDTO.fromEntity(feedback);
    }

    /**
     * 家长回复反馈
     */
    @Transactional
    public FeedbackDTO respondToFeedback(Long id, FeedbackDTO.ResponseRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User parent = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found: " + id));

        // 设置回复内容
        feedback.setParentResponse(request.getParentResponse());
        feedback.setRespondedBy(parent);
        feedback.setRespondedAt(LocalDateTime.now());

        // 如果指定了状态，更新状态
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            feedback.setStatus(Feedback.Status.valueOf(request.getStatus()));
        } else if (feedback.getStatus() == Feedback.Status.PENDING) {
            // 默认将待处理状态改为已查看
            feedback.setStatus(Feedback.Status.REVIEWED);
        }

        feedbackRepository.save(feedback);
        return FeedbackDTO.fromEntity(feedback);
    }

    /**
     * 更新反馈状态
     */
    @Transactional
    public FeedbackDTO updateStatus(Long id, String status) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found: " + id));

        feedback.setStatus(Feedback.Status.valueOf(status));
        feedbackRepository.save(feedback);
        return FeedbackDTO.fromEntity(feedback);
    }

    /**
     * 获取反馈统计信息
     */
    @Transactional(readOnly = true)
    public FeedbackDTO.StatsDTO getStats() {
        long total = feedbackRepository.count();
        List<Object[]> statusCounts = feedbackRepository.countByStatus();

        FeedbackDTO.StatsDTO stats = FeedbackDTO.StatsDTO.builder()
                .total(total)
                .build();

        for (Object[] row : statusCounts) {
            Feedback.Status status = (Feedback.Status) row[0];
            long count = (long) row[1];
            switch (status) {
                case PENDING -> stats.setPending(count);
                case REVIEWED -> stats.setReviewed(count);
                case ACCEPTED -> stats.setAccepted(count);
                case REJECTED -> stats.setRejected(count);
            }
        }

        return stats;
    }

    /**
     * 获取待处理的反馈列表
     */
    @Transactional(readOnly = true)
    public List<FeedbackDTO> getPendingFeedbacks() {
        return feedbackRepository.findPendingFeedbacks().stream()
                .map(FeedbackDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
