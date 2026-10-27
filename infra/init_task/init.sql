-- MySQL dump 10.13  Distrib 26.7.0, for macos26.6 (arm64)
--
-- Host: localhost    Database: online_shopping
-- ------------------------------------------------------
-- Server version	26.7.0

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
-- Current Database: `online_shopping`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `online_shopping` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `online_shopping`;

--
-- Table structure for table `online_shopping_commodity`
--


/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `online_shopping_commodity` (
  `commodity_id` bigint NOT NULL AUTO_INCREMENT,
  `commodity_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `commodity_desc` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `price` int NOT NULL,
  `available_stock` int NOT NULL,
  `creator_user_id` bigint NOT NULL,
  `total_stock` int NOT NULL DEFAULT '0',
  `lock_stock` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`commodity_id`) USING BTREE,
  KEY `creator_user_id` (`creator_user_id`),
  CONSTRAINT `online_shopping_commodity_ibfk_1` FOREIGN KEY (`creator_user_id`) REFERENCES `online_shopping_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1010 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `online_shopping_order`
--


/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `online_shopping_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `order_status` int NOT NULL,
  `commodity_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `order_amount` decimal(10,0) unsigned NOT NULL,
  `create_time` datetime NOT NULL,
  `pay_time` datetime DEFAULT NULL,
  PRIMARY KEY (`order_id`) USING BTREE,
  KEY `commodity_id` (`commodity_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `online_shopping_order_ibfk_1` FOREIGN KEY (`commodity_id`) REFERENCES `online_shopping_commodity` (`commodity_id`),
  CONSTRAINT `online_shopping_order_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `online_shopping_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `online_shopping_user`
--

CREATE TABLE IF NOT EXISTS `online_shopping_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
  `user_type` int NOT NULL COMMENT '用户类型 1 buyer 2 seller',
  `name` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户名',
  `email` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '用户名',
  `address` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '地址',
  `phone` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '手机号',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

INSERT INTO `online_shopping_user` (`user_id`, `user_type`, `name`, `email`, `address`, `phone`) VALUES
(1, 2, 'testSeller', 'testSeller@example.com', '123 Seller St', '1234567890');

/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'online_shopping'
--

--
-- Dumping routines for database 'online_shopping'
--

--
-- Current Database: `online_shopping_1`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `online_shopping_1` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `online_shopping_1`;

--
-- Table structure for table `online_shopping_order_1`
--

CREATE TABLE IF NOT EXISTS `online_shopping_order_1` (
  `order_id` bigint NOT NULL,
  `order_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_status` int NOT NULL,
  `commodity_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `order_amount` decimal(10,0) unsigned NOT NULL,
  `create_time` datetime NOT NULL,
  `pay_time` datetime DEFAULT NULL,
  PRIMARY KEY (`order_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `online_shopping_order_2`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `online_shopping_order_2` (
  `order_id` bigint NOT NULL,
  `order_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_status` int NOT NULL,
  `commodity_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `order_amount` decimal(10,0) unsigned NOT NULL,
  `create_time` datetime NOT NULL,
  `pay_time` datetime DEFAULT NULL,
  PRIMARY KEY (`order_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'online_shopping_1'
--

--
-- Dumping routines for database 'online_shopping_1'
--

--
-- Current Database: `online_shopping_2`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `online_shopping_2` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `online_shopping_2`;

--
-- Table structure for table `online_shopping_order_1`
--

CREATE TABLE IF NOT EXISTS `online_shopping_order_1` (
  `order_id` bigint NOT NULL,
  `order_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_status` int NOT NULL,
  `commodity_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `order_amount` decimal(10,0) unsigned NOT NULL,
  `create_time` datetime NOT NULL,
  `pay_time` datetime DEFAULT NULL,
  PRIMARY KEY (`order_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `online_shopping_order_2`
--

CREATE TABLE IF NOT EXISTS `online_shopping_order_2` (
  `order_id` bigint NOT NULL,
  `order_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_status` int NOT NULL,
  `commodity_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `order_amount` decimal(10,0) unsigned NOT NULL,
  `create_time` datetime NOT NULL,
  `pay_time` datetime DEFAULT NULL,
  PRIMARY KEY (`order_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'online_shopping_2'
--

--
-- Dumping routines for database 'online_shopping_2'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-13 19:09:36
