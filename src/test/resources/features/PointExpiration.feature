# Point Expiration Feature Test Scenarios

Feature: Point Expiration System
  As a parent
  I want points earned by my child to expire after a certain period
  So that I can encourage timely use of points and balance earning/consumption

  Background:
    Given the point expiration system is enabled with 180 days expiration
    And there is a child "小明" with parent "parent"

  Scenario: Points are added with expiration date
    When "小明" completes a task and earns 50 points
    Then the points should be added to wallet with expiration date 180 days from now
    And the total available points should be 50

  Scenario: Points are spent using FIFO method
    Given "小明" has multiple point batches:
      | points | earned_date  | expiration_date |
      | 100    | 2024-01-01   | 2024-06-29      |
      | 50     | 2024-02-01   | 2024-07-30      |
    When "小明" spends 120 points
    Then 100 points should be spent from the first batch (oldest)
    And 20 points should be spent from the second batch
    And the remaining points in second batch should be 30

  Scenario: Points expire after expiration date
    Given "小明" has a point batch with 100 points earned on 2024-01-01
    And the expiration date is 2024-06-29 (180 days)
    When the current date is 2024-06-30
    And the expiration check runs
    Then the points should be marked as expired
    And the remaining points should be 0
    And the total available points should not include expired points

  Scenario: Expired points cannot be spent
    Given "小明" has an expired point batch with 100 points
    When "小明" tries to spend 50 points
    Then the spending should fail
    And 0 points should be spent

  Scenario: Child can see points expiring soon
    Given "小明" has point batches expiring in:
      | points | days_until_expiration |
      | 50     | 7                     |
      | 100    | 15                    |
      | 200    | 30                    |
    When "小明" checks points expiring within 14 days
    Then should see 50 points expiring in 7 days
    And should see 100 points expiring in 15 days if checking 30 days

  Scenario: Partial spending from a batch
    Given "小明" has a point batch with 100 points
    When "小明" spends 30 points
    Then the batch should have 70 remaining points
    And the batch should not be marked as fully spent

  Scenario: Batch marked as fully spent when exhausted
    Given "小明" has a point batch with 50 points
    When "小明" spends 50 points
    Then the batch should be marked as fully spent
    And the remaining points should be 0

  Scenario: Expiration can be disabled
    Given the point expiration system is disabled
    When "小明" earns 100 points
    Then the points should have no expiration date
    And the points should never expire

  Scenario: Different expiration periods for different sources
    Given task completion points expire in 180 days
    And bonus points expire in 30 days
    When "小明" completes a task and earns 50 points
    And "小明" receives a bonus of 100 points
    Then task points should expire in 180 days
    And bonus points should expire in 30 days

  Scenario: Daily automatic expiration check
    Given there are expired point batches in the system
    When the scheduled job runs at 2 AM
    Then all expired batches should be marked as expired
    And notifications should be sent to children about expired points

  Scenario: Wallet shows point history with expiration
    Given "小明" has earned and spent points over time
    When "小明" views point wallet
    Then should see all point batches with:
      | Field |
      | Earned Date |
      | Original Points |
      | Remaining Points |
      | Expiration Date |
      | Status (Active/Expired/Spent) |

  Scenario: Insufficient points due to expiration
    Given "小明" had 200 points total
    And 100 points have expired
    When "小明" tries to redeem a reward costing 150 points
    Then the redemption should fail due to insufficient points
    And available points should be 100
