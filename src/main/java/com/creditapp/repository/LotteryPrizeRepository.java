package com.creditapp.repository;

import com.creditapp.entity.LotteryPrize;
import com.creditapp.entity.LotteryTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LotteryPrizeRepository extends JpaRepository<LotteryPrize, Long> {
    
    /**
     * 查询抽奖主题下的所有奖品
     */
    List<LotteryPrize> findByLotteryTheme(LotteryTheme lotteryTheme);
    
    /**
     * 查询抽奖主题下所有启用的奖品
     */
    List<LotteryPrize> findByLotteryThemeAndActiveTrue(LotteryTheme lotteryTheme);
    
    /**
     * 查询抽奖主题下所有启用的奖品（按权重降序）
     */
    @Query("SELECT lp FROM LotteryPrize lp WHERE lp.lotteryTheme.id = :themeId AND lp.active = true ORDER BY lp.weight DESC")
    List<LotteryPrize> findActivePrizesByThemeId(@Param("themeId") Long themeId);
}
