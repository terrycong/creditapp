package com.creditapp.service.impl;

import com.creditapp.config.PointExpirationProperties;
import com.creditapp.entity.Child;
import com.creditapp.entity.PointWallet;
import com.creditapp.repository.PointWalletRepository;
import com.creditapp.service.PointWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PointWalletServiceImpl implements PointWalletService {

    private final PointWalletRepository pointWalletRepository;
    private final PointExpirationProperties expirationProperties;

    @Override
    public PointWallet addPoints(Child child, int points, String sourceType, Long sourceId, int expirationDays) {
        log.info("Adding {} points to child {} wallet from {}", points, child.getUsername(), sourceType);

        // Use default expiration days if not specified
        if (expirationDays <= 0 && expirationProperties.isEnabled()) {
            expirationDays = expirationProperties.getExpirationDays();
        }

        LocalDate expirationDate = null;
        if (expirationProperties.isEnabled() && expirationDays > 0) {
            expirationDate = LocalDate.now().plusDays(expirationDays);
            log.info("Points will expire on: {}", expirationDate);
        }

        PointWallet wallet = PointWallet.builder()
                .child(child)
                .originalPoints(points)
                .remainingPoints(points)
                .earnedDate(LocalDateTime.now())
                .expirationDate(expirationDate)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .fullySpent(false)
                .expired(false)
                .build();

        wallet = pointWalletRepository.save(wallet);
        log.info("Point wallet entry created: id={}, child={}, points={}, expires={}", 
                wallet.getId(), child.getUsername(), points, expirationDate);
        
        return wallet;
    }

    @Override
    public int spendPoints(Child child, int points) {
        log.info("Spending {} points for child {}", points, child.getUsername());

        if (points <= 0) {
            log.warn("Attempted to spend invalid points amount: {}", points);
            return 0;
        }

        // Get all available point batches ordered by earned date (FIFO)
        List<PointWallet> availableBatches = pointWalletRepository.findAvailableBatches(child);

        if (availableBatches.isEmpty()) {
            log.warn("No available points for child {}", child.getUsername());
            return 0;
        }

        int remainingToSpend = points;
        int totalSpent = 0;

        for (PointWallet batch : availableBatches) {
            if (remainingToSpend <= 0) {
                break;
            }

            int spentFromBatch = batch.spendPoints(remainingToSpend);
            if (spentFromBatch > 0) {
                remainingToSpend -= spentFromBatch;
                totalSpent += spentFromBatch;
                pointWalletRepository.save(batch);
                log.debug("Spent {} points from batch {}, remaining to spend: {}", 
                        spentFromBatch, batch.getId(), remainingToSpend);
            }
        }

        if (totalSpent < points) {
            log.info("Could only spend {} of {} requested points for child {}", 
                    totalSpent, points, child.getUsername());
        } else {
            log.info("Successfully spent {} points for child {}", totalSpent, child.getUsername());
        }

        return totalSpent;
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalPoints(Child child) {
        Integer total = pointWalletRepository.getTotalPointsByChild(child);
        return total != null ? total : 0;
    }

    @Override
    @Scheduled(cron = "${points.expiration.check-cron:0 0 2 * * ?}")
    public int checkAndExpirePoints() {
        if (!expirationProperties.isEnabled()) {
            log.debug("Point expiration is disabled");
            return 0;
        }

        log.info("Checking for ALL expired points (not just today)...");
        LocalDate today = LocalDate.now();
        
        // Find ALL expired batches, not just ones that expired today
        // This handles cases where the scheduled job missed previous days
        List<PointWallet> expiredBatches = pointWalletRepository.findAllExpiredPoints(today);
        
        int expiredCount = 0;
        int totalExpiredPoints = 0;
        
        for (PointWallet batch : expiredBatches) {
            int expiredPoints = batch.getRemainingPoints();
            batch.markAsExpired();
            pointWalletRepository.save(batch);
            expiredCount++;
            totalExpiredPoints += expiredPoints;
            
            log.info("Expired {} points from batch {} for child {} (expired on {}, processed on {})", 
                    expiredPoints, batch.getId(), batch.getChild().getUsername(), 
                    batch.getExpirationDate(), today);
        }

        if (expiredCount > 0) {
            log.warn("Point expiration check complete: {} batches expired with {} total points. " +
                    "Note: This includes points that may have expired on previous days if job missed runs.", 
                    expiredCount, totalExpiredPoints);
        } else {
            log.info("Point expiration check complete: No expired points found");
        }
        
        return expiredCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointWallet> getPointsExpiringSoon(Child child, int days) {
        LocalDate maxDate = LocalDate.now().plusDays(days);
        return pointWalletRepository.findExpiringPoints(child, maxDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointWallet> getAllPointBatches(Child child) {
        return pointWalletRepository.findByChildOrderByEarnedDateAsc(child);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalExpiredPoints(Child child) {
        Integer total = pointWalletRepository.getTotalExpiredPoints(child);
        return total != null ? total : 0;
    }
}
