package com.creditapp.dto;

import com.creditapp.entity.Feedback;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 反馈数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackDTO {

    private Long id;
    private Long childId;
    private String childName;
    private String category;
    private String categoryDisplay;
    private String title;
    private String description;
    private String status;
    private String statusDisplay;
    private String parentResponse;
    private Long respondedById;
    private String respondedByName;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为 DTO
     */
    public static FeedbackDTO fromEntity(Feedback feedback) {
        return FeedbackDTO.builder()
                .id(feedback.getId())
                .childId(feedback.getChild() != null ? feedback.getChild().getId() : null)
                .childName(feedback.getChild() != null ? feedback.getChild().getUsername() : null)
                .category(feedback.getCategory() != null ? feedback.getCategory().name() : null)
                .categoryDisplay(feedback.getCategory() != null ? feedback.getCategory().getDisplayName() : null)
                .title(feedback.getTitle())
                .description(feedback.getDescription())
                .status(feedback.getStatus() != null ? feedback.getStatus().name() : null)
                .statusDisplay(feedback.getStatus() != null ? feedback.getStatus().getDisplayName() : null)
                .parentResponse(feedback.getParentResponse())
                .respondedById(feedback.getRespondedBy() != null ? feedback.getRespondedBy().getId() : null)
                .respondedByName(feedback.getRespondedBy() != null ? feedback.getRespondedBy().getUsername() : null)
                .respondedAt(feedback.getRespondedAt())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }

    /**
     * 提交反馈的请求 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmitRequest {
        private String category;  // FUNCTIONAL, NON_FUNCTIONAL, OTHER
        private String title;
        private String description;
    }

    /**
     * 回复反馈的请求 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResponseRequest {
        private String parentResponse;
        private String status;  // REVIEWED, ACCEPTED, REJECTED
    }

    /**
     * 统计信息 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatsDTO {
        private long total;
        private long pending;
        private long reviewed;
        private long accepted;
        private long rejected;
    }
}
