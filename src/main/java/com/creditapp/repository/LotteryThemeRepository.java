package com.creditapp.repository;

import com.creditapp.entity.LotteryTheme;
import com.creditapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LotteryThemeRepository extends JpaRepository<LotteryTheme, Long> {
    
    /**
     * 查询家长创建的所有抽奖主题
     */
    List<LotteryTheme> findByCreatedBy(User createdBy);
    
    /**
     * 查询家长创建的所有启用中的抽奖主题
     */
    List<LotteryTheme> findByCreatedByAndActiveTrue(User createdBy);
    
    /**
     * 查询所有启用的抽奖主题（供小孩查看）
     */
    @Query("SELECT lt FROM LotteryTheme lt WHERE lt.active = true ORDER BY lt.id DESC")
    List<LotteryTheme> findAllActiveThemes();
    
    /**
     * 查询所有启用的抽奖主题，并预加载奖品列表
     */
    @Query("SELECT lt FROM LotteryTheme lt LEFT JOIN FETCH lt.prizes WHERE lt.active = true ORDER BY lt.id DESC")
    List<LotteryTheme> findAllActiveThemesWithPrizes();
}
