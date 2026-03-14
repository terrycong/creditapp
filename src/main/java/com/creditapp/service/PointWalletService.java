package com.creditapp.service;

import com.creditapp.entity.Child;
import com.creditapp.entity.PointWallet;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PointWalletService {

    /**
     * Add points to child's wallet (new batch)
     * @param child child who earned points
     * @param points points earned
     * @param sourceType source type (TASK_COMPLETION, BONUS, etc.)
     * @param sourceId source entity ID
     * @param expirationDays expiration period in days (-1 for no expiration)
     * @return created PointWallet entry
     */
    PointWallet addPoints(Child child, int points, String sourceType, Long sourceId, int expirationDays);

    /**
     * Spend points from child's wallet (FIFO method)
     * @param child child who is spending points
     * @param points points to spend
     * @return actual points spent
     */
    int spendPoints(Child child, int points);

    /**
     * Get total available points for a child
     * @param child child
     * @return total remaining points (not expired, not fully spent)
     */
    int getTotalPoints(Child child);

    /**
     * Check and mark expired points
     * @return number of point batches expired
     */
    int checkAndExpirePoints();

    /**
     * Get points expiring within N days
     * @param child child
     * @param days days to check
     * @return list of point batches expiring soon
     */
    List<PointWallet> getPointsExpiringSoon(Child child, int days);

    /**
     * Get all point batches for a child
     * @param child child
     * @return list of all point batches
     */
    List<PointWallet> getAllPointBatches(Child child);

    /**
     * Get total expired points for a child
     * @param child child
     * @return total expired points
     */
    int getTotalExpiredPoints(Child child);
}
