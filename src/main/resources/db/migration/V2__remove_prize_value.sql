-- Migration: Remove prize_value column from lottery tables
-- Date: 2026-03-23
-- Description: Remove prize_value field from lottery prizes and draw results

-- Drop prize_value column from lottery_prizes table
ALTER TABLE lottery_prizes DROP COLUMN IF EXISTS prize_value;

-- Drop prize_value column from lottery_draw_results table  
ALTER TABLE lottery_draw_results DROP COLUMN IF EXISTS prize_value;
