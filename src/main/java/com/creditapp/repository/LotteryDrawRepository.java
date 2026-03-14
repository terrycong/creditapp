package com.creditapp.repository;

import com.creditapp.entity.Child;
import com.creditapp.entity.LotteryDraw;
import com.creditapp.entity.LotteryTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LotteryDrawRepository extends JpaRepository<LotteryDraw, Long> {
    
    /**
     * 查询小孩的所有抽奖记录
     */
    List<LotteryDraw> findByChild(Child child);
    
    /**
     * 查询小孩的所有抽奖记录（按时间倒序）
     */
    @Query("SELECT ld FROM LotteryDraw ld LEFT JOIN FETCH ld.drawResults WHERE ld.child.id = :childId ORDER BY ld.drawAt DESC")
    List<LotteryDraw> findByChildIdOrderByDrawAtDesc(@Param("childId") Long childId);
    
    /**
     * 查询指定时间范围内的抽奖记录
     */
    @Query("SELECT ld FROM LotteryDraw ld WHERE ld.child.id = :childId AND ld.drawAt >= :startTime AND ld.drawAt <= :endTime ORDER BY ld.drawAt DESC")
    List<LotteryDraw> findByChildIdAndTimeRange(@Param("childId") Long childId, 
                                                 @Param("startTime") LocalDateTime startTime, 
                                                 @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询抽奖主题下的所有抽奖记录
     */
    List<LotteryDraw> findByLotteryTheme(LotteryTheme lotteryTheme);
    
    /**
     * 统计小孩在指定时间范围内的抽奖次数
     */
    @Query("SELECT COUNT(ld) FROM LotteryDraw ld WHERE ld.child.id = :childId AND ld.drawAt >= :startTime AND ld.drawAt <= :endTime")
    long countByChildIdAndTimeRange(@Param("childId") Long childId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计小孩在指定时间范围内消耗的总积分
     */
    @Query("SELECT SUM(ld.pointsCost) FROM LotteryDraw ld WHERE ld.child.id = :childId AND ld.drawAt >= :startTime AND ld.drawAt <= :endTime")
    Integer sumPointsCostByChildIdAndTimeRange(@Param("childId") Long childId,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);
}
