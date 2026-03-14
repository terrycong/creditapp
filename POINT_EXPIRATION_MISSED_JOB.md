# Point Expiration - Missed Job Handling

## Problem Scenario

**Question**: What happens if the 2 AM scheduled job misses for several days due to server issues?

## Solution: Catch-All Expiration Logic

The expiration check is designed to handle missed runs gracefully:

```java
@Scheduled(cron = "${points.expiration.check-cron:0 0 2 * * ?}")
public int checkAndExpirePoints() {
    LocalDate today = LocalDate.now();
    
    // Find ALL expired batches, not just ones that expired today
    List<PointWallet> expiredBatches = pointWalletRepository.findAllExpiredPoints(today);
    
    // Expire ALL batches that are past their expiration date
    for (PointWallet batch : expiredBatches) {
        batch.markAsExpired();
    }
}
```

### Key Design Decision

**Query**: `WHERE expiration_date < :today AND expired = false`

This query finds:
- ✅ Batches that expired today
- ✅ Batches that expired yesterday
- ✅ Batches that expired last week
- ✅ ALL batches that have expired but not been marked

### Timeline Example

```
Day 1 (Jan 1): Batch A expires (job missed)
Day 2 (Jan 2): Batch B expires (job missed)
Day 3 (Jan 3): Batch C expires (job missed)
Day 4 (Jan 4): Job DOWN
Day 5 (Jan 5): Job DOWN
Day 6 (Jan 6): Job RUNNING

Result on Day 6:
- Batch A: EXPIRED ✅
- Batch B: EXPIRED ✅
- Batch C: EXPIRED ✅
```

### Log Output

```
INFO: Expired 100 points from batch 1 for child 小明 (expired on 2024-01-01, processed on 2024-01-06)
INFO: Expired 50 points from batch 2 for child 小明 (expired on 2024-01-02, processed on 2024-01-06)
INFO: Expired 75 points from batch 3 for child 小明 (expired on 2024-01-03, processed on 2024-01-06)
WARN: Point expiration check complete: 3 batches expired with 225 total points.
      Note: This includes points that may have expired on previous days if job missed runs.
```

### Guarantees

1. **No Lost Expirations** - Every expired batch will be caught eventually
2. **Accurate Tracking** - Logs show when it expired vs when it was processed
3. **Idempotent** - Running multiple times is safe (already expired batches are skipped)
4. **Transparent** - Clear logging indicates if job missed previous runs

### Testing

**Integration Test**: `PointExpirationIntegrationTest.testMissedJobScenarios()`

```java
@Test
void testMissedJobScenarios() {
    // Create batches that expired on different days in the past
    createPointBatch(child, 100, today.minusDays(185), today.minusDays(5));
    createPointBatch(child, 50, today.minusDays(183), today.minusDays(3));
    createPointBatch(child, 75, today.minusDays(181), today.minusDays(1));
    
    // Run expiration check
    int expiredCount = pointWalletService.checkAndExpirePoints();
    
    // ALL 3 batches should expire (not just today's)
    assertThat(expiredCount).isEqualTo(3);
}
```

### Configuration Options

**Increase Frequency** (more resilient to missed runs):
```properties
# Check every 12 hours instead of daily
points.expiration.check-cron=0 0 */12 * * ?
```

**Disable Temporarily** (for maintenance):
```properties
points.expiration.check-cron=-
```

**Change Time** (avoid peak hours):
```properties
# Run at 4 AM instead of 2 AM
points.expiration.check-cron=0 0 4 * * ?
```

### Monitoring Recommendations

1. **Set up alerts** for job failures
2. **Monitor logs** for "job missed runs" warnings
3. **Track expiration trends** (should be consistent day-to-day)
4. **Review weekly** to ensure no systemic issues

### Conclusion

The expiration system is **fault-tolerant by design**. Even if the scheduled job misses multiple days, **all expired points will be caught** on the next successful run. No manual intervention required!

---

**Updated**: 2026-03-14
**Documented in**: AGENTS.md (积分过期系统 section)
