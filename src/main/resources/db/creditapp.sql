-- MySQL dump 10.13  Distrib 8.4.8, for Linux (x86_64)
--
-- Host: 192.168.9.113    Database: creditapp
-- ------------------------------------------------------
-- Server version	8.4.7

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `children`
--

DROP TABLE IF EXISTS `children`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `children` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `username` varchar(50) NOT NULL,
                            `password` varchar(255) NOT NULL,
                            `role` enum('CHILD','PARENT') NOT NULL,
                            `points` int NOT NULL DEFAULT '0',
                            `parent_id` bigint NOT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `username` (`username`),
                            KEY `FK_children_parent` (`parent_id`),
                            CONSTRAINT `FK_children_parent` FOREIGN KEY (`parent_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf16 COMMENT='Child accounts with parent relationship';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `children`
--

LOCK TABLES `children` WRITE;
/*!40000 ALTER TABLE `children` DISABLE KEYS */;
INSERT INTO `children` VALUES (1,'child','$2a$10$cV03s.di3hDvqXLPMiAvpucc7aLcLrWv5kHMFWIrOZXWdAtT4SsDi','CHILD',120,1);
/*!40000 ALTER TABLE `children` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupons`
--

DROP TABLE IF EXISTS `coupons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupons` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
                           `code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Coupon code',
                           `points` int NOT NULL COMMENT 'Points value',
                           `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Is enabled',
                           `comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Comment/note',
                           `username` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Assigned username (optional)',
                           `timeout_seconds` int NOT NULL COMMENT 'Timeout in seconds',
                           `used_count` int NOT NULL DEFAULT '0' COMMENT 'Usage count',
                           `redeemed` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Is redeemed',
                           `redeemed_by` bigint DEFAULT NULL COMMENT 'Redeemed by user ID',
                           `redeemed_at` datetime DEFAULT NULL COMMENT 'Redeemed timestamp',
                           `created_by` bigint DEFAULT NULL COMMENT 'Created by user ID',
                           `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created timestamp',
                           `inserted_at` datetime DEFAULT NULL COMMENT 'Inserted timestamp',
                           `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated timestamp',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `code` (`code`),
                           KEY `fk_coupons_redeemed_by` (`redeemed_by`),
                           KEY `idx_coupons_code` (`code`),
                           KEY `idx_coupons_enabled` (`enabled`),
                           KEY `idx_coupons_username` (`username`),
                           KEY `idx_coupons_redeemed` (`redeemed`),
                           KEY `idx_coupons_created_by` (`created_by`),
                           CONSTRAINT `fk_coupons_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
                           CONSTRAINT `fk_coupons_redeemed_by` FOREIGN KEY (`redeemed_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Internet access coupons';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupons`
--

LOCK TABLES `coupons` WRITE;
/*!40000 ALTER TABLE `coupons` DISABLE KEYS */;
/*!40000 ALTER TABLE `coupons` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feedbacks`
--

DROP TABLE IF EXISTS `feedbacks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedbacks` (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `child_id` bigint NOT NULL,
                             `category` enum('FUNCTIONAL','NON_FUNCTIONAL','OTHER') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FUNCTIONAL=功能建议，NON_FUNCTIONAL=非功能建议，OTHER=其他',
                             `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
                             `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
                             `status` enum('PENDING','REVIEWED','ACCEPTED','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
                             `parent_response` text COLLATE utf8mb4_unicode_ci,
                             `responded_by_id` bigint DEFAULT NULL,
                             `responded_at` datetime DEFAULT NULL,
                             `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`),
                             KEY `FK_feedbacks_responder` (`responded_by_id`),
                             KEY `idx_feedbacks_child` (`child_id`),
                             KEY `idx_feedbacks_status` (`status`),
                             KEY `idx_feedbacks_category` (`category`),
                             KEY `idx_feedbacks_created` (`created_at`),
                             CONSTRAINT `FK_feedbacks_child` FOREIGN KEY (`child_id`) REFERENCES `children` (`id`) ON DELETE CASCADE,
                             CONSTRAINT `FK_feedbacks_responder` FOREIGN KEY (`responded_by_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Child feedback and suggestions';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedbacks`
--

LOCK TABLES `feedbacks` WRITE;
/*!40000 ALTER TABLE `feedbacks` DISABLE KEYS */;
INSERT INTO `feedbacks` VALUES (1,1,'FUNCTIONAL','希望能有更多奖励选择','现在的奖励有点少，希望能增加一些新的奖励，比如更多的游戏时间或者新的玩具','PENDING',NULL,NULL,NULL,'2026-04-08 17:07:54','2026-04-08 17:07:54');
INSERT INTO `feedbacks` VALUES (2,1,'NON_FUNCTIONAL','界面颜色可以更漂亮一些','现在的界面颜色比较简单，希望能有更多颜色选择，让我可以自定义喜欢的主题','REVIEWED',NULL,NULL,NULL,'2026-04-08 17:07:54','2026-04-08 17:07:54');
INSERT INTO `feedbacks` VALUES (3,1,'OTHER','谢谢爸爸妈妈','这个系统很好用，让我可以清楚地看到自己的任务和奖励，谢谢爸爸妈妈的用心','ACCEPTED',NULL,NULL,NULL,'2026-04-08 17:07:54','2026-04-08 17:07:54');
/*!40000 ALTER TABLE `feedbacks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lottery_draw_results`
--

DROP TABLE IF EXISTS `lottery_draw_results`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lottery_draw_results` (
                                        `id` bigint NOT NULL AUTO_INCREMENT,
                                        `lottery_draw_id` bigint NOT NULL,
                                        `prize_id` bigint NOT NULL,
                                        `reward_id` bigint DEFAULT NULL,
                                        `prize_name` varchar(100) NOT NULL,
                                        `created_at` datetime NOT NULL,
                                        PRIMARY KEY (`id`),
                                        KEY `FK_results_draw` (`lottery_draw_id`),
                                        KEY `FK_results_prize` (`prize_id`),
                                        KEY `FK_results_reward` (`reward_id`),
                                        CONSTRAINT `FK_results_draw` FOREIGN KEY (`lottery_draw_id`) REFERENCES `lottery_draws` (`id`),
                                        CONSTRAINT `FK_results_prize` FOREIGN KEY (`prize_id`) REFERENCES `lottery_prizes` (`id`),
                                        CONSTRAINT `FK_results_reward` FOREIGN KEY (`reward_id`) REFERENCES `rewards` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf16 COMMENT='Lottery draw result details';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lottery_draw_results`
--

LOCK TABLES `lottery_draw_results` WRITE;
/*!40000 ALTER TABLE `lottery_draw_results` DISABLE KEYS */;
INSERT INTO `lottery_draw_results` VALUES (1,1,5,14,'10 分钟上网券','2026-04-11 06:30:40');
/*!40000 ALTER TABLE `lottery_draw_results` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lottery_draws`
--

DROP TABLE IF EXISTS `lottery_draws`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lottery_draws` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `lottery_theme_id` bigint NOT NULL,
                                 `child_id` bigint NOT NULL,
                                 `points_cost` int NOT NULL,
                                 `result_status` enum('JACKPOT','NO_WIN','WON') NOT NULL,
                                 `draw_at` datetime NOT NULL,
                                 `created_at` datetime NOT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY `FK_draws_theme` (`lottery_theme_id`),
                                 KEY `FK_draws_child` (`child_id`),
                                 CONSTRAINT `FK_draws_child` FOREIGN KEY (`child_id`) REFERENCES `children` (`id`),
                                 CONSTRAINT `FK_draws_theme` FOREIGN KEY (`lottery_theme_id`) REFERENCES `lottery_themes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf16 COMMENT='Lottery draw records';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lottery_draws`
--

LOCK TABLES `lottery_draws` WRITE;
/*!40000 ALTER TABLE `lottery_draws` DISABLE KEYS */;
INSERT INTO `lottery_draws` VALUES (1,2,1,50,'WON','2026-04-11 06:30:40','2026-04-11 06:30:40');
/*!40000 ALTER TABLE `lottery_draws` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lottery_prizes`
--

DROP TABLE IF EXISTS `lottery_prizes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lottery_prizes` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `lottery_theme_id` bigint NOT NULL,
                                  `reward_id` bigint DEFAULT NULL,
                                  `name` varchar(100) NOT NULL,
                                  `description` varchar(500) DEFAULT NULL,
                                  `image_url` varchar(500) DEFAULT NULL,
                                  `weight` int NOT NULL,
                                  `probability` int DEFAULT NULL,
                                  `quantity` int NOT NULL DEFAULT '-1',
                                  `redeemed_count` int NOT NULL DEFAULT '0',
                                  `active` bit(1) NOT NULL DEFAULT b'1',
                                  `created_at` datetime NOT NULL,
                                  `updated_at` datetime DEFAULT NULL,
                                  PRIMARY KEY (`id`),
                                  KEY `FK_prizes_theme` (`lottery_theme_id`),
                                  KEY `FK_prizes_reward` (`reward_id`),
                                  CONSTRAINT `FK_prizes_reward` FOREIGN KEY (`reward_id`) REFERENCES `rewards` (`id`),
                                  CONSTRAINT `FK_prizes_theme` FOREIGN KEY (`lottery_theme_id`) REFERENCES `lottery_themes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf16 COMMENT='Lottery prize definitions';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lottery_prizes`
--

LOCK TABLES `lottery_prizes` WRITE;
/*!40000 ALTER TABLE `lottery_prizes` DISABLE KEYS */;
INSERT INTO `lottery_prizes` VALUES (5,2,14,'10 分钟上网券','上网 10 分钟',NULL,30,NULL,999,1,_binary '','2026-04-07 16:57:33','2026-04-11 06:30:40');
INSERT INTO `lottery_prizes` VALUES (6,2,18,'雪糕一个','','',30,NULL,999,0,_binary '','2026-04-07 16:57:33','2026-04-07 17:07:44');
INSERT INTO `lottery_prizes` VALUES (7,2,23,'看电影一部','','',40,NULL,999,0,_binary '','2026-04-07 16:57:33','2026-04-07 17:07:53');
INSERT INTO `lottery_prizes` VALUES (8,2,22,'住酒店一晚','','',1,NULL,999,0,_binary '','2026-04-07 16:57:33','2026-04-07 17:03:23');
INSERT INTO `lottery_prizes` VALUES (9,2,34,'游戏时间','30 分钟游戏时间',NULL,30,NULL,999,0,_binary '','2026-04-08 17:07:54',NULL);
INSERT INTO `lottery_prizes` VALUES (10,2,36,'冰淇淋','喜欢的冰淇淋一份',NULL,50,NULL,999,0,_binary '','2026-04-08 17:07:54',NULL);
INSERT INTO `lottery_prizes` VALUES (11,2,37,'零花钱','10 元零花钱',NULL,15,NULL,999,0,_binary '','2026-04-08 17:07:54',NULL);
INSERT INTO `lottery_prizes` VALUES (12,2,38,'新玩具','买一个喜欢的玩具',NULL,5,NULL,999,0,_binary '','2026-04-08 17:07:54',NULL);
/*!40000 ALTER TABLE `lottery_prizes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lottery_themes`
--

DROP TABLE IF EXISTS `lottery_themes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lottery_themes` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `name` varchar(100) NOT NULL,
                                  `description` varchar(500) DEFAULT NULL,
                                  `points_per_draw` int NOT NULL,
                                  `type` enum('FIXED_PROBABILITY','GUARANTEED','WEIGHTED_RANDOM') NOT NULL,
                                  `active` bit(1) NOT NULL DEFAULT b'1',
                                  `created_by_id` bigint NOT NULL,
                                  `created_at` datetime NOT NULL,
                                  `updated_at` datetime DEFAULT NULL,
                                  PRIMARY KEY (`id`),
                                  KEY `FK_themes_created_by` (`created_by_id`),
                                  CONSTRAINT `FK_themes_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf16 COMMENT='Lottery theme definitions';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lottery_themes`
--

LOCK TABLES `lottery_themes` WRITE;
/*!40000 ALTER TABLE `lottery_themes` DISABLE KEYS */;
INSERT INTO `lottery_themes` VALUES (2,'幸运大转盘','每日抽奖机会，试试你的运气！',50,'WEIGHTED_RANDOM',_binary '',1,'2026-04-07 16:57:33','2026-04-07 17:03:06');
/*!40000 ALTER TABLE `lottery_themes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_preferences`
--

DROP TABLE IF EXISTS `notification_preferences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_preferences` (
                                            `id` bigint NOT NULL AUTO_INCREMENT,
                                            `user_id` bigint NOT NULL,
                                            `task_reminder` bit(1) NOT NULL DEFAULT b'1',
                                            `reward_approved` bit(1) NOT NULL DEFAULT b'1',
                                            `penalty_applied` bit(1) NOT NULL DEFAULT b'1',
                                            `system_announcement` bit(1) NOT NULL DEFAULT b'1',
                                            `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                            PRIMARY KEY (`id`),
                                            UNIQUE KEY `user_id` (`user_id`),
                                            CONSTRAINT `FK_notification_preferences_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User notification preferences';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_preferences`
--

LOCK TABLES `notification_preferences` WRITE;
/*!40000 ALTER TABLE `notification_preferences` DISABLE KEYS */;
INSERT INTO `notification_preferences` VALUES (1,1,_binary '',_binary '',_binary '',_binary '','2026-04-08 17:07:54');
/*!40000 ALTER TABLE `notification_preferences` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_reads`
--

DROP TABLE IF EXISTS `notification_reads`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_reads` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
                                      `notification_id` bigint NOT NULL COMMENT 'Notification ID',
                                      `child_id` bigint NOT NULL COMMENT 'Child user ID',
                                      `read_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Read timestamp',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_notification_child` (`notification_id`,`child_id`),
                                      KEY `idx_reads_child` (`child_id`),
                                      KEY `idx_reads_notification` (`notification_id`),
                                      CONSTRAINT `fk_reads_child` FOREIGN KEY (`child_id`) REFERENCES `children` (`id`) ON DELETE CASCADE,
                                      CONSTRAINT `fk_reads_notification` FOREIGN KEY (`notification_id`) REFERENCES `notifications` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Notification read status tracking';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_reads`
--

LOCK TABLES `notification_reads` WRITE;
/*!40000 ALTER TABLE `notification_reads` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification_reads` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
                                 `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Notification title',
                                 `content` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Notification content',
                                 `created_by_id` bigint NOT NULL COMMENT 'Created by parent user ID',
                                 `target_type` enum('ALL','SPECIFIC_CHILD') COLLATE utf8mb4_unicode_ci DEFAULT 'ALL' COMMENT 'Target audience',
                                 `target_child_id` bigint DEFAULT NULL COMMENT 'Target child ID (if SPECIFIC_CHILD)',
                                 `priority` enum('LOW','NORMAL','HIGH','URGENT') COLLATE utf8mb4_unicode_ci DEFAULT 'NORMAL' COMMENT 'Priority level',
                                 `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Is active',
                                 `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created timestamp',
                                 `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated timestamp',
                                 PRIMARY KEY (`id`),
                                 KEY `fk_notifications_created_by` (`created_by_id`),
                                 KEY `fk_notifications_target_child` (`target_child_id`),
                                 KEY `idx_notifications_created_at` (`created_at` DESC),
                                 KEY `idx_notifications_active` (`active`),
                                 KEY `idx_notifications_target` (`target_type`,`target_child_id`),
                                 CONSTRAINT `fk_notifications_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `users` (`id`),
                                 CONSTRAINT `fk_notifications_target_child` FOREIGN KEY (`target_child_id`) REFERENCES `children` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Family notifications';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,'欢迎来到','欢迎来到积分系统',1,'ALL',NULL,'NORMAL',1,'2026-04-07 16:59:16','2026-04-07 16:59:16');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `penalty_notifications`
--

DROP TABLE IF EXISTS `penalty_notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `penalty_notifications` (
                                         `id` bigint NOT NULL AUTO_INCREMENT,
                                         `child_id` bigint NOT NULL,
                                         `parent_id` bigint NOT NULL,
                                         `task_id` bigint NOT NULL,
                                         `status` enum('APPLIED','DISMISSED','EXPIRED','PENDING') NOT NULL,
                                         `notification_time` datetime NOT NULL,
                                         `penalty_points` int DEFAULT NULL,
                                         `is_penalty_applied` bit(1) DEFAULT NULL,
                                         `applied_by_id` bigint DEFAULT NULL,
                                         `applied_at` datetime DEFAULT NULL,
                                         PRIMARY KEY (`id`),
                                         KEY `FK_notifications_child` (`child_id`),
                                         KEY `FK_notifications_parent` (`parent_id`),
                                         KEY `FK_notifications_task` (`task_id`),
                                         KEY `FK_notifications_applied_by` (`applied_by_id`),
                                         CONSTRAINT `FK_notifications_applied_by` FOREIGN KEY (`applied_by_id`) REFERENCES `users` (`id`),
                                         CONSTRAINT `FK_notifications_child` FOREIGN KEY (`child_id`) REFERENCES `children` (`id`),
                                         CONSTRAINT `FK_notifications_parent` FOREIGN KEY (`parent_id`) REFERENCES `users` (`id`),
                                         CONSTRAINT `FK_notifications_task` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf16 COMMENT='Penalty notification records';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `penalty_notifications`
--

LOCK TABLES `penalty_notifications` WRITE;
/*!40000 ALTER TABLE `penalty_notifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `penalty_notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `penalty_records`
--

DROP TABLE IF EXISTS `penalty_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `penalty_records` (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `child_id` bigint NOT NULL,
                                   `penalty_rule_id` bigint NOT NULL,
                                   `points` int NOT NULL,
                                   `note` varchar(500) DEFAULT NULL,
                                   `applied_by_id` bigint NOT NULL,
                                   `applied_at` datetime NOT NULL,
                                   PRIMARY KEY (`id`),
                                   KEY `FK_records_child` (`child_id`),
                                   KEY `FK_records_rule` (`penalty_rule_id`),
                                   KEY `FK_records_applied_by` (`applied_by_id`),
                                   CONSTRAINT `FK_records_applied_by` FOREIGN KEY (`applied_by_id`) REFERENCES `users` (`id`),
                                   CONSTRAINT `FK_records_child` FOREIGN KEY (`child_id`) REFERENCES `children` (`id`),
                                   CONSTRAINT `FK_records_rule` FOREIGN KEY (`penalty_rule_id`) REFERENCES `penalty_rules` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf16 COMMENT='Executed penalty records';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `penalty_records`
--

LOCK TABLES `penalty_records` WRITE;
/*!40000 ALTER TABLE `penalty_records` DISABLE KEYS */;
INSERT INTO `penalty_records` VALUES (1,1,10,50,'偷玩平板',1,'2026-04-11 13:51:11');
INSERT INTO `penalty_records` VALUES (2,1,9,5,'',1,'2026-04-13 14:24:08');
INSERT INTO `penalty_records` VALUES (3,1,1,5,'',1,'2026-04-14 14:20:12');
/*!40000 ALTER TABLE `penalty_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `penalty_rules`
--

DROP TABLE IF EXISTS `penalty_rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `penalty_rules` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `name` varchar(100) NOT NULL,
                                 `description` varchar(500) DEFAULT NULL,
                                 `points` int NOT NULL,
                                 `active` bit(1) NOT NULL DEFAULT b'1',
                                 `created_by_id` bigint NOT NULL,
                                 `created_at` datetime NOT NULL,
                                 `updated_at` datetime DEFAULT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY `FK_rules_created_by` (`created_by_id`),
                                 CONSTRAINT `FK_rules_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf16 COMMENT='Penalty rule definitions';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `penalty_rules`
--

LOCK TABLES `penalty_rules` WRITE;
/*!40000 ALTER TABLE `penalty_rules` DISABLE KEYS */;
INSERT INTO `penalty_rules` VALUES (1,'浪费牛奶','浪费牛奶',5,_binary '',1,'2026-04-07 16:57:33',NULL);
INSERT INTO `penalty_rules` VALUES (2,'浪费食物','浪费食物',10,_binary '',1,'2026-04-07 16:57:33',NULL);
INSERT INTO `penalty_rules` VALUES (3,'不讲卫生','不讲卫生',10,_binary '',1,'2026-04-07 16:57:33',NULL);
INSERT INTO `penalty_rules` VALUES (4,'不冲厕所','不冲厕所',10,_binary '',1,'2026-04-07 16:57:33',NULL);
INSERT INTO `penalty_rules` VALUES (5,'不讲礼貌','不讲礼貌',10,_binary '',1,'2026-04-07 16:57:33',NULL);
INSERT INTO `penalty_rules` VALUES (6,'不诚实','不诚实',30,_binary '',1,'2026-04-07 16:57:33',NULL);
INSERT INTO `penalty_rules` VALUES (7,'破坏财物','破坏财物',50,_binary '',1,'2026-04-07 16:57:33',NULL);
INSERT INTO `penalty_rules` VALUES (8,'不穿裤子','不穿裤子',5,_binary '',1,'2026-04-07 16:57:33',NULL);
INSERT INTO `penalty_rules` VALUES (9,'不穿拖鞋提醒后不立刻改正','不穿拖鞋提醒后不立刻改正',5,_binary '',1,'2026-04-07 16:57:33',NULL);
INSERT INTO `penalty_rules` VALUES (10,'不诚实使用平板','不诚实使用平板电脑，包括偷玩游戏、浏览不良内容等',50,_binary '',1,'2026-04-07 16:57:34',NULL);
INSERT INTO `penalty_rules` VALUES (11,'衣着不整','外出时衣着不整洁',10,_binary '',1,'2026-04-08 17:07:54',NULL);
/*!40000 ALTER TABLE `penalty_rules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `point_history`
--

DROP TABLE IF EXISTS `point_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `point_history` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `child_id` bigint NOT NULL,
                                 `change_type` enum('BONUS','INITIAL','LOTTERY_DRAW','LOTTERY_WIN','MANUAL_ADJUSTMENT','OTHER','PENALTY','REWARD_REDEMPTION','TASK_COMPLETION') NOT NULL,
                                 `change_points` int NOT NULL,
                                 `original_points` int NOT NULL,
                                 `after_points` int NOT NULL,
                                 `description` varchar(500) DEFAULT NULL,
                                 `reference_id` bigint DEFAULT NULL,
                                 `reference_type` varchar(50) DEFAULT NULL,
                                 `changed_by_id` bigint DEFAULT NULL,
                                 `created_at` datetime NOT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY `FK_history_child` (`child_id`),
                                 KEY `FK_history_changed_by` (`changed_by_id`),
                                 CONSTRAINT `FK_history_changed_by` FOREIGN KEY (`changed_by_id`) REFERENCES `users` (`id`),
                                 CONSTRAINT `FK_history_child` FOREIGN KEY (`child_id`) REFERENCES `children` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf16 COMMENT='Point change history log';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `point_history`
--

LOCK TABLES `point_history` WRITE;
/*!40000 ALTER TABLE `point_history` DISABLE KEYS */;
INSERT INTO `point_history` VALUES (1,1,'TASK_COMPLETION',5,0,5,'完成任务: 不赖床',1,'TASK_COMPLETION',NULL,'2026-04-08 15:50:50');
INSERT INTO `point_history` VALUES (2,1,'TASK_COMPLETION',5,5,10,'完成任务: 不赖床',2,'TASK_COMPLETION',NULL,'2026-04-10 14:21:09');
INSERT INTO `point_history` VALUES (3,1,'TASK_COMPLETION',10,10,20,'完成任务: 10:30 前上床睡觉',4,'TASK_COMPLETION',NULL,'2026-04-10 14:33:45');
INSERT INTO `point_history` VALUES (4,1,'TASK_COMPLETION',5,20,25,'完成任务: 上学不迟到',3,'TASK_COMPLETION',NULL,'2026-04-10 14:33:47');
INSERT INTO `point_history` VALUES (5,1,'TASK_COMPLETION',20,25,45,'完成任务: 配合打针',5,'TASK_COMPLETION',NULL,'2026-04-10 14:39:01');
INSERT INTO `point_history` VALUES (6,1,'TASK_COMPLETION',20,45,65,'完成任务: 配合打针',9,'TASK_COMPLETION',NULL,'2026-04-11 06:28:53');
INSERT INTO `point_history` VALUES (7,1,'TASK_COMPLETION',20,65,85,'完成任务: 配合打针',8,'TASK_COMPLETION',NULL,'2026-04-11 06:28:56');
INSERT INTO `point_history` VALUES (8,1,'TASK_COMPLETION',5,85,90,'完成任务: 不赖床',7,'TASK_COMPLETION',NULL,'2026-04-11 06:29:00');
INSERT INTO `point_history` VALUES (9,1,'TASK_COMPLETION',20,90,110,'完成任务: 配合打针',6,'TASK_COMPLETION',NULL,'2026-04-11 06:29:06');
INSERT INTO `point_history` VALUES (10,1,'LOTTERY_DRAW',-50,60,10,'抽奖消耗 - 幸运大转盘',2,'LOTTERY_THEME',NULL,'2026-04-11 06:30:40');
INSERT INTO `point_history` VALUES (11,1,'PENALTY',-50,60,10,'违规扣分: 不诚实使用平板 - 偷玩平板',NULL,'PENALTY',1,'2026-04-11 13:51:11');
INSERT INTO `point_history` VALUES (12,1,'REWARD_REDEMPTION',-10,10,0,'兑换礼物: 和爸爸看一个10分钟内的短视频',2,'REWARD_REDEMPTION',1,'2026-04-12 15:15:26');
INSERT INTO `point_history` VALUES (13,1,'TASK_COMPLETION',5,0,5,'完成任务: 不赖床',11,'TASK_COMPLETION',NULL,'2026-04-12 16:04:14');
INSERT INTO `point_history` VALUES (14,1,'TASK_COMPLETION',20,5,25,'完成任务: 配合打针',10,'TASK_COMPLETION',NULL,'2026-04-12 16:04:16');
INSERT INTO `point_history` VALUES (15,1,'PENALTY',-5,25,20,'违规扣分: 不穿拖鞋提醒后不立刻改正 - ',NULL,'PENALTY',1,'2026-04-13 14:24:08');
INSERT INTO `point_history` VALUES (16,1,'TASK_COMPLETION',5,20,25,'完成任务: 上学不迟到',15,'TASK_COMPLETION',NULL,'2026-04-13 15:14:11');
INSERT INTO `point_history` VALUES (17,1,'TASK_COMPLETION',20,25,45,'完成任务: 配合打针',14,'TASK_COMPLETION',NULL,'2026-04-13 15:14:15');
INSERT INTO `point_history` VALUES (18,1,'TASK_COMPLETION',5,45,50,'完成任务: 不赖床',13,'TASK_COMPLETION',NULL,'2026-04-13 15:14:20');
INSERT INTO `point_history` VALUES (19,1,'TASK_COMPLETION',5,50,55,'完成任务: 登记作业',12,'TASK_COMPLETION',NULL,'2026-04-13 15:14:22');
INSERT INTO `point_history` VALUES (20,1,'TASK_COMPLETION',10,55,65,'完成任务: 吃光早餐',16,'TASK_COMPLETION',NULL,'2026-04-13 15:15:41');
INSERT INTO `point_history` VALUES (21,1,'TASK_COMPLETION',5,65,70,'完成任务: 登记作业',22,'TASK_COMPLETION',NULL,'2026-04-14 14:18:16');
INSERT INTO `point_history` VALUES (22,1,'TASK_COMPLETION',15,70,85,'完成任务: 9:30 前冲完凉',21,'TASK_COMPLETION',NULL,'2026-04-14 14:18:21');
INSERT INTO `point_history` VALUES (23,1,'TASK_COMPLETION',5,85,90,'完成任务: 不赖床',20,'TASK_COMPLETION',NULL,'2026-04-14 14:18:30');
INSERT INTO `point_history` VALUES (24,1,'TASK_COMPLETION',5,90,95,'完成任务: 上学不迟到',19,'TASK_COMPLETION',NULL,'2026-04-14 14:19:27');
INSERT INTO `point_history` VALUES (25,1,'TASK_COMPLETION',20,95,115,'完成任务: 配合打针',18,'TASK_COMPLETION',NULL,'2026-04-14 14:19:31');
INSERT INTO `point_history` VALUES (26,1,'TASK_COMPLETION',10,115,125,'完成任务: 吃光早餐',17,'TASK_COMPLETION',NULL,'2026-04-14 14:19:35');
INSERT INTO `point_history` VALUES (27,1,'PENALTY',-5,125,120,'违规扣分: 浪费牛奶 - ',NULL,'PENALTY',1,'2026-04-14 14:20:12');
/*!40000 ALTER TABLE `point_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reward_redemptions`
--

DROP TABLE IF EXISTS `reward_redemptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reward_redemptions` (
                                      `id` bigint NOT NULL AUTO_INCREMENT,
                                      `reward_id` bigint NOT NULL,
                                      `child_id` bigint NOT NULL,
                                      `status` enum('REDEEMED','USED') NOT NULL,
                                      `redeemed_at` datetime NOT NULL,
                                      `used_at` datetime DEFAULT NULL,
                                      `note` varchar(500) DEFAULT NULL,
                                      PRIMARY KEY (`id`),
                                      KEY `FK_redemptions_reward` (`reward_id`),
                                      KEY `FK_redemptions_child` (`child_id`),
                                      CONSTRAINT `FK_redemptions_child` FOREIGN KEY (`child_id`) REFERENCES `children` (`id`),
                                      CONSTRAINT `FK_redemptions_reward` FOREIGN KEY (`reward_id`) REFERENCES `rewards` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf16 COMMENT='Reward redemption records';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reward_redemptions`
--

LOCK TABLES `reward_redemptions` WRITE;
/*!40000 ALTER TABLE `reward_redemptions` DISABLE KEYS */;
INSERT INTO `reward_redemptions` VALUES (1,14,1,'REDEEMED','2026-04-11 06:30:40',NULL,'抽奖中奖 - 10 分钟上网券');
INSERT INTO `reward_redemptions` VALUES (2,32,1,'USED','2026-04-12 15:15:26','2026-04-12 15:15:47',NULL);
/*!40000 ALTER TABLE `reward_redemptions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rewards`
--

DROP TABLE IF EXISTS `rewards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rewards` (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `name` varchar(100) NOT NULL,
                           `description` varchar(500) DEFAULT NULL,
                           `quantity` int NOT NULL DEFAULT '999',
                           `points_required` int NOT NULL,
                           `image_url` varchar(500) DEFAULT NULL,
                           `active` bit(1) NOT NULL DEFAULT b'1',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf16 COMMENT='Available rewards for children';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rewards`
--

LOCK TABLES `rewards` WRITE;
/*!40000 ALTER TABLE `rewards` DISABLE KEYS */;
INSERT INTO `rewards` VALUES (14,'10 分钟上网券','上网 10 分钟',998,50,NULL,_binary '');
INSERT INTO `rewards` VALUES (15,'20 分钟上网券','上网 20 分钟',999,100,NULL,_binary '');
INSERT INTO `rewards` VALUES (16,'30 分钟上网券','上网 30 分钟',999,150,NULL,_binary '');
INSERT INTO `rewards` VALUES (17,'1 小时上网券','上网 1 小时',999,200,NULL,_binary '');
INSERT INTO `rewards` VALUES (18,'雪糕一个','雪糕一个',999,60,NULL,_binary '');
INSERT INTO `rewards` VALUES (19,'零食一份','零食一份',999,60,NULL,_binary '');
INSERT INTO `rewards` VALUES (20,'饮料 1 支','饮料 1 支',999,60,NULL,_binary '');
INSERT INTO `rewards` VALUES (21,'挖洞洞 1 次','挖洞洞 1 次',999,120,NULL,_binary '');
INSERT INTO `rewards` VALUES (22,'住酒店一晚','住酒店一晚',999,500,NULL,_binary '');
INSERT INTO `rewards` VALUES (23,'看电影一部','看电影一部',999,100,NULL,_binary '');
INSERT INTO `rewards` VALUES (24,'和爸爸玩游戏半小时','和爸爸玩游戏半小时',999,120,NULL,_binary '');
INSERT INTO `rewards` VALUES (25,'去指定的餐厅吃饭','去指定的餐厅吃饭',999,50,NULL,_binary '');
INSERT INTO `rewards` VALUES (26,'下载故事 1 小时','下载故事 1 小时',999,50,NULL,_binary '');
INSERT INTO `rewards` VALUES (27,'免除责罚一次','免除责罚一次',999,200,NULL,_binary '');
INSERT INTO `rewards` VALUES (28,'免除扣分一次','免除扣分一次',999,100,NULL,_binary '');
INSERT INTO `rewards` VALUES (29,'一盒新模型','一盒新模型',999,300,NULL,_binary '');
INSERT INTO `rewards` VALUES (30,'和爸爸一起拼模型 1 小时','和爸爸一起拼模型 1 小时',999,20,NULL,_binary '');
INSERT INTO `rewards` VALUES (31,'和爸爸一起拼拼图 1 小时','和爸爸一起拼拼图 1 小时',999,30,NULL,_binary '');
INSERT INTO `rewards` VALUES (32,'和爸爸看一个10分钟内的短视频','和爸爸一起看一个10分钟内的短视频。备注：要等爸爸有空哦，而且每天只能用3次。',998,10,NULL,_binary '');
INSERT INTO `rewards` VALUES (33,'下载一个半小时的故事','下载一个半小时的故事',999,70,NULL,_binary '');
INSERT INTO `rewards` VALUES (34,'游戏时间','30 分钟游戏时间',999,20,NULL,_binary '');
INSERT INTO `rewards` VALUES (35,'看电影','选择一部喜欢的电影观看',999,40,NULL,_binary '');
INSERT INTO `rewards` VALUES (36,'冰淇淋','喜欢的冰淇淋一份',999,15,NULL,_binary '');
INSERT INTO `rewards` VALUES (37,'零花钱','10 元零花钱',999,100,NULL,_binary '');
INSERT INTO `rewards` VALUES (38,'新玩具','买一个喜欢的玩具',999,500,NULL,_binary '');
INSERT INTO `rewards` VALUES (39,'图书','购买一本喜欢的图书',999,40,NULL,_binary '');
INSERT INTO `rewards` VALUES (40,'文具套装','获得一套新文具',999,50,NULL,_binary '');
INSERT INTO `rewards` VALUES (41,'去游乐场','周末去游乐场玩半天',999,150,NULL,_binary '');
INSERT INTO `rewards` VALUES (42,'披萨大餐','全家一起吃披萨',999,100,NULL,_binary '');
INSERT INTO `rewards` VALUES (44,'选择周末活动','决定周末全家去哪里玩',999,80,NULL,_binary '');
INSERT INTO `rewards` VALUES (45,'晚睡 1 小时特权','周末可以晚睡 1 小时',999,40,NULL,_binary '');
INSERT INTO `rewards` VALUES (46,'免做家务一次','可以免除一次家务任务',999,25,NULL,_binary '');
INSERT INTO `rewards` VALUES (47,'和爸爸看一个 10 分钟内的短视频','和爸爸看一个 10 分钟内的短视频',999,10,NULL,_binary '');
INSERT INTO `rewards` VALUES (48,'看动画片半小时','观看动画片半小时',999,20,NULL,_binary '');
INSERT INTO `rewards` VALUES (49,'免罚站券半小时','免除半小时罚站惩罚',999,100,NULL,_binary '');
/*!40000 ALTER TABLE `rewards` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_completions`
--

DROP TABLE IF EXISTS `task_completions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_completions` (
                                    `id` bigint NOT NULL AUTO_INCREMENT,
                                    `task_id` bigint NOT NULL,
                                    `task_job_id` bigint DEFAULT NULL,
                                    `child_id` bigint NOT NULL,
                                    `status` enum('APPROVED','PENDING','REJECTED') NOT NULL,
                                    `proof` text,
                                    `completed_at` datetime NOT NULL,
                                    `approved_at` datetime DEFAULT NULL,
                                    PRIMARY KEY (`id`),
                                    KEY `FK_completions_task` (`task_id`),
                                    KEY `FK_completions_task_job` (`task_job_id`),
                                    KEY `FK_completions_child` (`child_id`),
                                    CONSTRAINT `FK_completions_child` FOREIGN KEY (`child_id`) REFERENCES `children` (`id`),
                                    CONSTRAINT `FK_completions_task` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
                                    CONSTRAINT `FK_completions_task_job` FOREIGN KEY (`task_job_id`) REFERENCES `task_jobs` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf16 COMMENT='Task completion records';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_completions`
--

LOCK TABLES `task_completions` WRITE;
/*!40000 ALTER TABLE `task_completions` DISABLE KEYS */;
INSERT INTO `task_completions` VALUES (1,8,5,1,'APPROVED',NULL,'2026-04-08 15:49:08','2026-04-08 15:50:50');
INSERT INTO `task_completions` VALUES (2,8,7,1,'APPROVED',NULL,'2026-04-10 14:20:58','2026-04-10 14:21:09');
INSERT INTO `task_completions` VALUES (3,14,8,1,'APPROVED',NULL,'2026-04-10 14:28:21','2026-04-10 14:33:47');
INSERT INTO `task_completions` VALUES (4,18,6,1,'APPROVED',NULL,'2026-04-10 14:28:26','2026-04-10 14:33:45');
INSERT INTO `task_completions` VALUES (5,13,9,1,'APPROVED',NULL,'2026-04-10 14:37:32','2026-04-10 14:39:01');
INSERT INTO `task_completions` VALUES (6,13,10,1,'APPROVED',NULL,'2026-04-11 06:14:16','2026-04-11 06:29:06');
INSERT INTO `task_completions` VALUES (7,8,11,1,'APPROVED',NULL,'2026-04-11 06:14:43','2026-04-11 06:29:00');
INSERT INTO `task_completions` VALUES (8,13,10,1,'APPROVED',NULL,'2026-04-11 06:14:45','2026-04-11 06:28:56');
INSERT INTO `task_completions` VALUES (9,13,10,1,'APPROVED',NULL,'2026-04-11 06:27:45','2026-04-11 06:28:53');
INSERT INTO `task_completions` VALUES (10,13,13,1,'APPROVED',NULL,'2026-04-12 15:16:50','2026-04-12 16:04:16');
INSERT INTO `task_completions` VALUES (11,8,12,1,'APPROVED',NULL,'2026-04-12 15:16:52','2026-04-12 16:04:14');
INSERT INTO `task_completions` VALUES (12,33,15,1,'APPROVED',NULL,'2026-04-13 15:13:45','2026-04-13 15:14:22');
INSERT INTO `task_completions` VALUES (13,8,16,1,'APPROVED',NULL,'2026-04-13 15:13:56','2026-04-13 15:14:20');
INSERT INTO `task_completions` VALUES (14,13,17,1,'APPROVED',NULL,'2026-04-13 15:14:01','2026-04-13 15:14:15');
INSERT INTO `task_completions` VALUES (15,14,18,1,'APPROVED',NULL,'2026-04-13 15:14:03','2026-04-13 15:14:11');
INSERT INTO `task_completions` VALUES (16,11,19,1,'APPROVED',NULL,'2026-04-13 15:15:36','2026-04-13 15:15:41');
INSERT INTO `task_completions` VALUES (17,11,25,1,'APPROVED',NULL,'2026-04-14 14:16:54','2026-04-14 14:19:35');
INSERT INTO `task_completions` VALUES (18,13,24,1,'APPROVED',NULL,'2026-04-14 14:16:56','2026-04-14 14:19:31');
INSERT INTO `task_completions` VALUES (19,14,23,1,'APPROVED',NULL,'2026-04-14 14:16:59','2026-04-14 14:19:27');
INSERT INTO `task_completions` VALUES (20,8,22,1,'APPROVED',NULL,'2026-04-14 14:17:01','2026-04-14 14:18:30');
INSERT INTO `task_completions` VALUES (21,17,21,1,'APPROVED',NULL,'2026-04-14 14:17:04','2026-04-14 14:18:21');
INSERT INTO `task_completions` VALUES (22,33,20,1,'APPROVED',NULL,'2026-04-14 14:17:09','2026-04-14 14:18:16');
INSERT INTO `task_completions` VALUES (23,28,14,1,'REJECTED',NULL,'2026-04-14 14:17:12',NULL);
/*!40000 ALTER TABLE `task_completions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_jobs`
--

DROP TABLE IF EXISTS `task_jobs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_jobs` (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `task_id` bigint NOT NULL,
                             `child_id` bigint NOT NULL,
                             `status` enum('ASSIGNED','CANCELLED','COMPLETED','IN_PROGRESS') NOT NULL,
                             `snapshot_title` varchar(100) NOT NULL,
                             `snapshot_description` varchar(500) DEFAULT NULL,
                             `snapshot_points` int NOT NULL,
                             `snapshot_task_type` enum('DAILY_ONCE','MANDATORY','ONE_TIME','REPEATABLE') NOT NULL,
                             `assigned_at` datetime NOT NULL,
                             `started_at` datetime DEFAULT NULL,
                             `deadline` datetime DEFAULT NULL,
                             `created_at` datetime NOT NULL,
                             `updated_at` datetime DEFAULT NULL,
                             PRIMARY KEY (`id`),
                             KEY `FK_task_jobs_task` (`task_id`),
                             KEY `FK_task_jobs_child` (`child_id`),
                             CONSTRAINT `FK_task_jobs_child` FOREIGN KEY (`child_id`) REFERENCES `children` (`id`),
                             CONSTRAINT `FK_task_jobs_task` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf16 COMMENT='Task instances assigned to children';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_jobs`
--

LOCK TABLES `task_jobs` WRITE;
/*!40000 ALTER TABLE `task_jobs` DISABLE KEYS */;
INSERT INTO `task_jobs` VALUES (5,8,1,'COMPLETED','不赖床','按时起床不赖床',5,'DAILY_ONCE','2026-04-08 15:48:38','2026-04-08 15:49:08',NULL,'2026-04-08 15:48:38','2026-04-08 15:50:50');
INSERT INTO `task_jobs` VALUES (6,18,1,'COMPLETED','10:30 前上床睡觉','晚上 10:30 前上床睡觉',10,'DAILY_ONCE','2026-04-10 14:18:28','2026-04-10 14:28:26',NULL,'2026-04-10 14:18:28','2026-04-10 14:33:45');
INSERT INTO `task_jobs` VALUES (7,8,1,'COMPLETED','不赖床','按时起床不赖床',5,'DAILY_ONCE','2026-04-10 14:18:58','2026-04-10 14:20:58',NULL,'2026-04-10 14:18:58','2026-04-10 14:21:09');
INSERT INTO `task_jobs` VALUES (8,14,1,'COMPLETED','上学不迟到','按时上学不迟到',5,'DAILY_ONCE','2026-04-10 14:28:10','2026-04-10 14:28:21',NULL,'2026-04-10 14:28:10','2026-04-10 14:33:47');
INSERT INTO `task_jobs` VALUES (9,13,1,'COMPLETED','配合打针','配合打针不哭闹',20,'REPEATABLE','2026-04-10 14:37:20','2026-04-10 14:37:32',NULL,'2026-04-10 14:37:20','2026-04-10 14:39:01');
INSERT INTO `task_jobs` VALUES (10,13,1,'COMPLETED','配合打针','配合打针不哭闹',20,'REPEATABLE','2026-04-11 06:14:08','2026-04-11 06:14:16',NULL,'2026-04-11 06:14:08','2026-04-11 06:28:53');
INSERT INTO `task_jobs` VALUES (11,8,1,'COMPLETED','不赖床','按时起床不赖床',5,'DAILY_ONCE','2026-04-11 06:14:33','2026-04-11 06:14:43',NULL,'2026-04-11 06:14:33','2026-04-11 06:29:00');
INSERT INTO `task_jobs` VALUES (12,8,1,'COMPLETED','不赖床','按时起床不赖床',5,'DAILY_ONCE','2026-04-12 15:16:16','2026-04-12 15:16:52',NULL,'2026-04-12 15:16:16','2026-04-12 16:04:14');
INSERT INTO `task_jobs` VALUES (13,13,1,'COMPLETED','配合打针','配合打针不哭闹',20,'REPEATABLE','2026-04-12 15:16:42','2026-04-12 15:16:50',NULL,'2026-04-12 15:16:42','2026-04-12 16:04:16');
INSERT INTO `task_jobs` VALUES (14,28,1,'IN_PROGRESS','考试 95 分以上','考试 95 分以上，奖励 200 积分',200,'REPEATABLE','2026-04-13 15:10:25','2026-04-14 14:17:12',NULL,'2026-04-13 15:10:25','2026-04-14 14:17:12');
INSERT INTO `task_jobs` VALUES (15,33,1,'COMPLETED','登记作业','',5,'DAILY_ONCE','2026-04-13 15:12:47','2026-04-13 15:13:45',NULL,'2026-04-13 15:12:47','2026-04-13 15:14:22');
INSERT INTO `task_jobs` VALUES (16,8,1,'COMPLETED','不赖床','按时起床不赖床',5,'DAILY_ONCE','2026-04-13 15:13:04','2026-04-13 15:13:56',NULL,'2026-04-13 15:13:04','2026-04-13 15:14:20');
INSERT INTO `task_jobs` VALUES (17,13,1,'COMPLETED','配合打针','配合打针不哭闹',20,'REPEATABLE','2026-04-13 15:13:23','2026-04-13 15:14:01',NULL,'2026-04-13 15:13:23','2026-04-13 15:14:15');
INSERT INTO `task_jobs` VALUES (18,14,1,'COMPLETED','上学不迟到','按时上学不迟到',5,'DAILY_ONCE','2026-04-13 15:13:33','2026-04-13 15:14:03',NULL,'2026-04-13 15:13:33','2026-04-13 15:14:11');
INSERT INTO `task_jobs` VALUES (19,11,1,'COMPLETED','吃光早餐','吃完早餐不浪费',10,'DAILY_ONCE','2026-04-13 15:15:15','2026-04-13 15:15:36',NULL,'2026-04-13 15:15:15','2026-04-13 15:15:41');
INSERT INTO `task_jobs` VALUES (20,33,1,'COMPLETED','登记作业','',5,'DAILY_ONCE','2026-04-14 14:12:10','2026-04-14 14:17:09',NULL,'2026-04-14 14:12:10','2026-04-14 14:18:16');
INSERT INTO `task_jobs` VALUES (21,17,1,'COMPLETED','9:30 前冲完凉','晚上 9:30 前洗完澡',15,'DAILY_ONCE','2026-04-14 14:12:26','2026-04-14 14:17:04',NULL,'2026-04-14 14:12:26','2026-04-14 14:18:21');
INSERT INTO `task_jobs` VALUES (22,8,1,'COMPLETED','不赖床','按时起床不赖床',5,'DAILY_ONCE','2026-04-14 14:12:34','2026-04-14 14:17:01',NULL,'2026-04-14 14:12:34','2026-04-14 14:18:30');
INSERT INTO `task_jobs` VALUES (23,14,1,'COMPLETED','上学不迟到','按时上学不迟到',5,'DAILY_ONCE','2026-04-14 14:12:51','2026-04-14 14:16:59',NULL,'2026-04-14 14:12:51','2026-04-14 14:19:27');
INSERT INTO `task_jobs` VALUES (24,13,1,'COMPLETED','配合打针','配合打针不哭闹',20,'REPEATABLE','2026-04-14 14:13:09','2026-04-14 14:16:56',NULL,'2026-04-14 14:13:09','2026-04-14 14:19:31');
INSERT INTO `task_jobs` VALUES (25,11,1,'COMPLETED','吃光早餐','吃完早餐不浪费',10,'DAILY_ONCE','2026-04-14 14:13:19','2026-04-14 14:16:54',NULL,'2026-04-14 14:13:19','2026-04-14 14:19:35');
/*!40000 ALTER TABLE `task_jobs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tasks`
--

DROP TABLE IF EXISTS `tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tasks` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `title` varchar(100) NOT NULL,
                         `description` varchar(500) DEFAULT NULL,
                         `points` int NOT NULL,
                         `type` enum('DAILY_ONCE','MANDATORY','ONE_TIME','REPEATABLE') NOT NULL,
                         `status` enum('APPROVED','DRAFT','REJECTED') DEFAULT NULL,
                         `active` bit(1) NOT NULL DEFAULT b'1',
                         `penalty_points` int DEFAULT NULL,
                         `deadline_type` enum('DAILY','WEEKLY_TIMES') DEFAULT NULL,
                         `deadline_value` int DEFAULT NULL,
                         `created_by_id` bigint DEFAULT NULL,
                         `created_at` datetime NOT NULL,
                         `updated_at` datetime DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         KEY `FK_tasks_created_by` (`created_by_id`),
                         CONSTRAINT `FK_tasks_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf16 COMMENT='Task definitions created by parents';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tasks`
--

LOCK TABLES `tasks` WRITE;
/*!40000 ALTER TABLE `tasks` DISABLE KEYS */;
INSERT INTO `tasks` VALUES (8,'不赖床','按时起床不赖床',5,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:33',NULL);
INSERT INTO `tasks` VALUES (9,'主动吃维生素','主动吃维生素',5,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:33',NULL);
INSERT INTO `tasks` VALUES (10,'没人叫就刷牙','主动刷牙不需要提醒',5,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:33',NULL);
INSERT INTO `tasks` VALUES (11,'吃光早餐','吃完早餐不浪费',10,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:33',NULL);
INSERT INTO `tasks` VALUES (12,'每天喝完两瓶牛奶','每天喝完两瓶牛奶',15,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:33',NULL);
INSERT INTO `tasks` VALUES (13,'配合打针','配合打针不哭闹',20,'REPEATABLE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:33',NULL);
INSERT INTO `tasks` VALUES (14,'上学不迟到','按时上学不迟到',5,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:33',NULL);
INSERT INTO `tasks` VALUES (15,'（非周末）回家前做完作业','非周末当天回家前完成作业',50,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:33',NULL);
INSERT INTO `tasks` VALUES (16,'9 点前做完作业','晚上 9 点前完成作业',20,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:33',NULL);
INSERT INTO `tasks` VALUES (17,'9:30 前冲完凉','晚上 9:30 前洗完澡',15,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:33',NULL);
INSERT INTO `tasks` VALUES (18,'10:30 前上床睡觉','晚上 10:30 前上床睡觉',10,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:33',NULL);
INSERT INTO `tasks` VALUES (19,'认真做一张课外练习卷','认真完成一张课外练习卷',60,'REPEATABLE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:33',NULL);
INSERT INTO `tasks` VALUES (20,'坚持背单词 10 天','连续坚持背单词 10 天，培养学习习惯',50,'ONE_TIME','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:34',NULL);
INSERT INTO `tasks` VALUES (21,'坚持背单词 20 天','连续坚持背单词 20 天，持之以恒',120,'ONE_TIME','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:34',NULL);
INSERT INTO `tasks` VALUES (22,'坚持背单词 50 天','连续坚持背单词 50 天，坚持不懈',300,'ONE_TIME','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:34',NULL);
INSERT INTO `tasks` VALUES (23,'一周学习 3 节宝典课','每周完成 3 节宝典课程学习',50,'REPEATABLE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-07 16:57:34',NULL);
INSERT INTO `tasks` VALUES (24,'每天做 30 道数学题','每天完成 30 道数学练习题，正确率达到 90% 或以上',10,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,NULL,'2026-04-07 16:57:34',NULL);
INSERT INTO `tasks` VALUES (25,'课余时间看半小时纪录片','课余时间观看半小时纪录片，增长知识',15,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,NULL,'2026-04-08 17:24:13',NULL);
INSERT INTO `tasks` VALUES (26,'看纪录片后做笔记','观看纪录片后写观后感或笔记，字数 100 字以上',15,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,NULL,'2026-04-08 17:24:13',NULL);
INSERT INTO `tasks` VALUES (27,'考试 90 分以上','考试 90 分以上，奖励 50 积分',50,'REPEATABLE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-08 18:00:14',NULL);
INSERT INTO `tasks` VALUES (28,'考试 95 分以上','考试 95 分以上，奖励 200 积分',200,'REPEATABLE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-08 18:00:14',NULL);
INSERT INTO `tasks` VALUES (30,'吃饭不离开座位','吃饭时保持坐姿不离开座位，直到吃完为止，培养良好用餐习惯',5,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,NULL,'2026-04-10 15:10:59',NULL);
INSERT INTO `tasks` VALUES (31,'跟爸爸一起学电脑','跟爸爸一起学习电脑知识半小时，培养计算机技能',25,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,NULL,'2026-04-10 17:02:57',NULL);
INSERT INTO `tasks` VALUES (32,'认真参与轮滑','认真参与轮滑运动一次，锻炼身体协调能力和运动技能',75,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,NULL,'2026-04-12 18:49:43',NULL);
INSERT INTO `tasks` VALUES (33,'登记作业','',5,'DAILY_ONCE','APPROVED',_binary '',NULL,NULL,NULL,1,'2026-04-13 15:11:34','2026-04-13 15:11:51');
/*!40000 ALTER TABLE `tasks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `username` varchar(50) NOT NULL,
                         `password` varchar(255) NOT NULL,
                         `role` enum('CHILD','PARENT') NOT NULL,
                         `points` int NOT NULL DEFAULT '0',
                         `parent_id` bigint DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf16 COMMENT='User accounts (parents and children)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'parent','$2a$10$BFeE1qUtBvthU1sKwEc.tOIzuFOw1D635dDscfC2cv1HcP1/Co0Su','PARENT',0,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-14 16:45:30
