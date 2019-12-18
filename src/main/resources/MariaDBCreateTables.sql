-- MySQL dump 10.16  Distrib 10.1.43-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: lastfm
-- ------------------------------------------------------
-- Server version	10.1.43-MariaDB-0ubuntu0.18.04.1
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `album_crowns`
--

DROP TABLE IF EXISTS `album_crowns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `album_crowns` (
                                `artist_id` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL,
                                `discordId` bigint(20) NOT NULL,
                                `album` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL,
                                `plays` int(11) NOT NULL,
                                `guildID` bigint(20) NOT NULL,
                                PRIMARY KEY (`artist_id`,`album`,`guildID`),
                                -- UNIQUE KEY `artist_id_UNIQUE` (`artist_id`,`album`,`guildID`),
                                KEY `album_crown_fK_guildID` (`discordId`,`guildID`),
                                CONSTRAINT `album_crown_fK_guildID` FOREIGN KEY (`discordId`, `guildID`) REFERENCES `user_guild` (`discordId`, `guildId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `artist`
--

DROP TABLE IF EXISTS `artist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artist` (
                          `artist_ID` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL,
                          `playNumber` int(11) DEFAULT NULL,
                          `lastFMID` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
                          PRIMARY KEY (`artist_ID`,`lastFMID`),
                          KEY `artist_idx_lastfmid_id` (`lastFMID`,`artist_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `artist_url`
--

DROP TABLE IF EXISTS `artist_url`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `artist_url` (
                              `artist_ID` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL,
                              `url` varchar(180) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                              `url_status` tinyint(1) DEFAULT '1',
                              `correction_status` tinyint(1) DEFAULT '0',
                              PRIMARY KEY (`artist_ID`),
                              UNIQUE KEY `artist_url_artist_id_uindex` (`artist_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `corrections`
--

DROP TABLE IF EXISTS `corrections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `corrections` (
                               `artist_id` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                               `correction` varchar(191) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                               UNIQUE KEY `correction_UNIQUE` (`correction`),
                               UNIQUE KEY `artist_id_UNIQUE` (`artist_id`),
                               CONSTRAINT `a_id_fk` FOREIGN KEY (`correction`) REFERENCES `artist_url` (`artist_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guild_logo`
--

DROP TABLE IF EXISTS `guild_logo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guild_logo` (
                              `guildId` bigint(20) NOT NULL,
                              `logo` blob,
                              PRIMARY KEY (`guildId`),
                              UNIQUE KEY `guildId_UNIQUE` (`guildId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `guild_prefixes`
--

DROP TABLE IF EXISTS `guild_prefixes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guild_prefixes` (
                                  `guildId` bigint(20) NOT NULL DEFAULT '0',
                                  `prefix` char(1) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '!',
                                  PRIMARY KEY (`guildId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lastfm`
--

DROP TABLE IF EXISTS `lastfm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lastfm` (
                          `discordID` bigint(20) NOT NULL,
                          `lastFmId` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                          PRIMARY KEY (`discordID`),
                          UNIQUE KEY `lastfm_pk` (`lastFmId`),
                          KEY `lastfm_lastFmId_index` (`lastFmId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `metrics`
--

DROP TABLE IF EXISTS `metrics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metrics` (
                           `id` int(11) NOT NULL AUTO_INCREMENT,
                           `name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                           `value` bigint(20) DEFAULT '0',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `randomlinks`
--

DROP TABLE IF EXISTS `randomlinks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `randomlinks` (
                               `discordId` bigint(20) NOT NULL,
                               `guildId` bigint(20) DEFAULT NULL,
                               `url` varchar(191) COLLATE utf8mb4_general_ci NOT NULL,
                               PRIMARY KEY (`url`),
                               UNIQUE KEY `unique_url_random` (`url`),
                               KEY `discordId` (`discordId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `test`
--

DROP TABLE IF EXISTS `test`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test` (
    `c` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `updated`
--

DROP TABLE IF EXISTS `updated`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `updated` (
                           `discordID` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
                           `last_update` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                           `control_timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (`discordID`),
                           CONSTRAINT `lastfmIdFK` FOREIGN KEY (`discordID`) REFERENCES `lastfm` (`lastFmId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_guild`
--

DROP TABLE IF EXISTS `user_guild`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_guild` (
                              `discordId` bigint(20) NOT NULL,
                              `guildId` bigint(20) NOT NULL,
                              PRIMARY KEY (`discordId`,`guildId`),
                              KEY `user_guild_idx_guildid` (`guildId`),
                              CONSTRAINT `user_guild_lastfm_discordID_fk` FOREIGN KEY (`discordId`) REFERENCES `lastfm` (`discordID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-12-18 22:52:28
