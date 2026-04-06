package com.creditapp.repository;

import com.creditapp.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 反馈数据访问层
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * 分页查询某个孩子的所有反馈
     */
    Page<Feedback> findByChildId(Long childId, Pageable pageable);

    /**
     * 查询某个孩子的所有反馈（按创建时间倒序）
     */
    List<Feedback> findByChildIdOrderByCreatedAtDesc(Long childId);

    /**
     * 分页查询所有反馈（按创建时间倒序）
     */
    Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 按状态筛选反馈
     */
    Page<Feedback> findByStatus(Feedback.Status status, Pageable pageable);

    /**
     * 按类型筛选反馈
     */
    Page<Feedback> findByCategory(Feedback.Category category, Pageable pageable);

    /**
     * 统计某个孩子的反馈数量
     */
    long countByChildId(Long childId);

    /**
     * 统计各状态的反馈数量
     */
    @Query("SELECT f.status, COUNT(f) FROM Feedback f GROUP BY f.status")
    List<Object[]> countByStatus();

    /**
     * 查询待处理的反馈
     */
    @Query("SELECT f FROM Feedback f WHERE f.status = 'PENDING' ORDER BY f.createdAt ASC")
    List<Feedback> findPendingFeedbacks();
}
