CREATE DATABASE IF NOT EXISTS `lastfm` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */;
USE `lastfm`;
-- MySQL dump 10.13  Distrib 8.0.14, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: lastfm
-- ------------------------------------------------------
-- Server version	8.0.14

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
SET NAMES utf8;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

--
-- Table structure for table `artist`
--

DROP TABLE IF EXISTS `artist`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
SET character_set_client = utf8mb4;
CREATE TABLE `artist`
(
    `artist_id`  varchar(200) NOT NULL,
    `playNumber` int(11) DEFAULT NULL,
    `lastFMID`   varchar(45)  NOT NULL,
    PRIMARY KEY (`artist_id`, `lastFMID`),
    KEY `artist_idx_lastfmid_id` (`lastFMID`, `artist_id`),
    CONSTRAINT `artist_lastfm_lastFmId_fk` FOREIGN KEY (`lastFMID`) REFERENCES `lastfm` (`lastFmId`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `artist_url`
--

DROP TABLE IF EXISTS `artist_url`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
SET character_set_client = utf8mb4;
CREATE TABLE `artist_url`
(
    `artist_id`  varchar(200) NOT NULL,
    `url`        varchar(180) DEFAULT NULL,
    `url_status` tinyint(1)   DEFAULT '1',
    PRIMARY KEY (`artist_id`),
    UNIQUE KEY `artist_url_artist_id_uindex` (`artist_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guild_logo`
--

DROP TABLE IF EXISTS `guild_logo`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
SET character_set_client = utf8mb4;
CREATE TABLE `guild_logo`
(
    `guildId` bigint(20) NOT NULL,
    `logo`    blob,
    PRIMARY KEY (`guildId`),
    UNIQUE KEY `guildId_UNIQUE` (`guildId`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lastfm`
--

DROP TABLE IF EXISTS `lastfm`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
SET character_set_client = utf8mb4;
CREATE TABLE `lastfm`
(
    `discordID` bigint(20) NOT NULL,
    `lastFmId`  varchar(45) DEFAULT NULL,
    PRIMARY KEY (`discordID`),
    UNIQUE KEY `lastfm_pk` (`lastFmId`),
    KEY `lastfm_lastFmId_index` (`lastFmId`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updated`
--

DROP TABLE IF EXISTS `updated`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
SET character_set_client = utf8mb4;
CREATE TABLE `updated`
(
    `discordID`   varchar(45) NOT NULL,
    `last_update` timestamp   NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`discordID`),
    CONSTRAINT `lastfmIdFK` FOREIGN KEY (`discordID`) REFERENCES `lastfm` (`lastFmId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_guild`
--

DROP TABLE IF EXISTS `user_guild`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
SET character_set_client = utf8mb4;
CREATE TABLE `user_guild`
(
    `discordId` bigint(20) NOT NULL,
    `guildId`   bigint(20) NOT NULL,
    PRIMARY KEY (`discordId`, `guildId`),
    KEY `user_guild_idx_guildid` (`guildId`),
    CONSTRAINT `user_guild_lastfm_discordID_fk` FOREIGN KEY (`discordId`) REFERENCES `lastfm` (`discordID`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2019-06-19 15:12:05
