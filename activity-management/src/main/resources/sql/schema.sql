-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: huoji
-- ------------------------------------------------------
-- Server version	8.0.41

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
-- Current Database: `huoji`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `huoji` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `huoji`;

--
-- Table structure for table `activities`
--

DROP TABLE IF EXISTS `activities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activities` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '活动唯一标识符，自增主键',
  `at_name` varchar(100) NOT NULL COMMENT '活动名称',
  `time` varchar(50) NOT NULL COMMENT '活动举办时间',
  `location` varchar(255) NOT NULL COMMENT '活动举办地点',
  `content` text COMMENT '活动详细内容描述',
  `promoter` int NOT NULL COMMENT '活动发起人，关联 users 表的 id',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `num` int DEFAULT NULL COMMENT '活动人数',
  `a_price` int DEFAULT NULL COMMENT '总费用',
  `male_price` int DEFAULT NULL COMMENT '男性费用',
  `female_price` int DEFAULT NULL COMMENT '女性费用',
  `price_by_gender` tinyint(1) DEFAULT '0' COMMENT '是否按性别收费',
  PRIMARY KEY (`id`),
  KEY `fk_promoter` (`promoter`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_invitations`
--

DROP TABLE IF EXISTS `activity_invitations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_invitations` (
  `id` int NOT NULL AUTO_INCREMENT,
  `activity_id` int NOT NULL COMMENT '活动ID',
  `inviter_id` int NOT NULL COMMENT '邀请人ID',
  `invitee_id` int NOT NULL COMMENT '被邀请人ID',
  `status` varchar(20) DEFAULT 'pending' COMMENT '邀请状态：pending/accepted/rejected',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `responded_at` datetime DEFAULT NULL COMMENT '响应时间',
  PRIMARY KEY (`id`),
  KEY `activity_id` (`activity_id`),
  KEY `inviter_id` (`inviter_id`),
  KEY `invitee_id` (`invitee_id`),
  CONSTRAINT `activity_invitations_ibfk_1` FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`),
  CONSTRAINT `activity_invitations_ibfk_2` FOREIGN KEY (`inviter_id`) REFERENCES `users` (`id`),
  CONSTRAINT `activity_invitations_ibfk_3` FOREIGN KEY (`invitee_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动邀请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_participants`
--

DROP TABLE IF EXISTS `activity_participants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_participants` (
  `id` int NOT NULL AUTO_INCREMENT,
  `activity_id` int NOT NULL COMMENT '活动ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `joined_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `payment_status` enum('not_paid','paid') DEFAULT 'not_paid' COMMENT '支付状态',
  `payment_amount` int DEFAULT NULL COMMENT '支付金额（分）',
  `join_type` varchar(20) DEFAULT 'self_join' COMMENT '参与方式：self_join/invited',
  `paid_at` datetime DEFAULT NULL COMMENT '支付时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_activity_user` (`activity_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `activity_participants_ibfk_1` FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`) ON DELETE CASCADE,
  CONSTRAINT `activity_participants_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动参与表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_payments`
--

DROP TABLE IF EXISTS `activity_payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_payments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `activity_id` int NOT NULL COMMENT '活动ID',
  `promoter_id` int NOT NULL COMMENT '发起人ID',
  `amount` int NOT NULL COMMENT '收款金额（分）',
  `description` varchar(255) DEFAULT NULL COMMENT '收款描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `activity_id` (`activity_id`),
  KEY `promoter_id` (`promoter_id`),
  CONSTRAINT `activity_payments_ibfk_1` FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`) ON DELETE CASCADE,
  CONSTRAINT `activity_payments_ibfk_2` FOREIGN KEY (`promoter_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动收款表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `draw_items`
--

DROP TABLE IF EXISTS `draw_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `draw_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `draw_id` int NOT NULL,
  `name` varchar(255) NOT NULL COMMENT 'draw item name',
  `count` int NOT NULL COMMENT 'people count for this item',
  PRIMARY KEY (`id`),
  KEY `idx_draw_item_draw` (`draw_id`),
  CONSTRAINT `fk_draw_items_draw` FOREIGN KEY (`draw_id`) REFERENCES `draws` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='draw content items';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `draw_participants`
--

DROP TABLE IF EXISTS `draw_participants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `draw_participants` (
  `id` int NOT NULL AUTO_INCREMENT,
  `draw_id` int NOT NULL,
  `user_id` int NOT NULL,
  `result` int DEFAULT NULL COMMENT 'random draw number',
  `draw_item_id` int DEFAULT NULL COMMENT 'assigned draw item',
  `joined_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_draw_user` (`draw_id`,`user_id`),
  KEY `idx_draw_participant_user` (`user_id`),
  KEY `fk_draw_participants_item` (`draw_item_id`),
  CONSTRAINT `fk_draw_participants_draw` FOREIGN KEY (`draw_id`) REFERENCES `draws` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_draw_participants_item` FOREIGN KEY (`draw_item_id`) REFERENCES `draw_items` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_draw_participants_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='draw participants';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `draws`
--

DROP TABLE IF EXISTS `draws`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `draws` (
  `id` int NOT NULL AUTO_INCREMENT,
  `content` varchar(500) NOT NULL COMMENT 'draw content',
  `people_count` int NOT NULL COMMENT 'participant capacity',
  `visibility` varchar(20) NOT NULL DEFAULT 'public' COMMENT 'public/private',
  `invite_code` char(4) NOT NULL COMMENT 'unique 4-digit code',
  `creator_id` int NOT NULL COMMENT 'creator user id',
  `status` varchar(20) NOT NULL DEFAULT 'open' COMMENT 'open/drawn/expired',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `expires_at` datetime NOT NULL COMMENT 'code valid until',
  `drawn_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_draw_invite_code` (`invite_code`),
  KEY `idx_draw_creator` (`creator_id`),
  CONSTRAINT `fk_draw_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='draws table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_blacklist`
--

DROP TABLE IF EXISTS `user_blacklist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_blacklist` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户ID（拉黑人）',
  `blacklisted_user_id` int NOT NULL COMMENT '被拉黑用户ID',
  `reason` varchar(500) DEFAULT NULL COMMENT 'ban reason',
  `banned_until` datetime DEFAULT NULL COMMENT 'ban expiry, NULL means permanent',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_blacklist` (`user_id`,`blacklisted_user_id`),
  KEY `blacklisted_user_id` (`blacklisted_user_id`),
  CONSTRAINT `user_blacklist_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `user_blacklist_ibfk_2` FOREIGN KEY (`blacklisted_user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户黑名单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_follows`
--

DROP TABLE IF EXISTS `user_follows`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_follows` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户ID（关注人）',
  `followed_user_id` int NOT NULL COMMENT '被关注用户ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_follows` (`user_id`,`followed_user_id`),
  KEY `followed_user_id` (`followed_user_id`),
  CONSTRAINT `user_follows_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `user_follows_ibfk_2` FOREIGN KEY (`followed_user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `openid` varchar(255) DEFAULT NULL COMMENT '微信openid',
  `nickname` varchar(255) DEFAULT NULL COMMENT '用户昵称',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  `gender` varchar(10) DEFAULT 'unknown' COMMENT '性别：male/female/unknown',
  `role` varchar(20) DEFAULT 'user' COMMENT '用户角色：admin/user',
  PRIMARY KEY (`id`),
  UNIQUE KEY `openid` (`openid`),
  UNIQUE KEY `phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'huoji'
--

--
-- Dumping routines for database 'huoji'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-31 11:39:18
