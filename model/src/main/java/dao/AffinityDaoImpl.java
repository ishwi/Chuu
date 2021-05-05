package dao;

import dao.entities.*;
import dao.exceptions.ChuuServiceException;
import dao.musicbrainz.AffinityDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AffinityDaoImpl implements AffinityDao {
    @Override
    public void initTempTable(Connection connection, String ownerLastfmID, String receiverLastFMId, long threshold) {
        String queryBody =
                """
                        CREATE TEMPORARY TABLE affinity
                        SELECT *
                        FROM   (SELECT artist_id AS a1,\s
                                       a.lastfm_id AS l1,\s
                                       a.playnumber AS p1
                                FROM   scrobbled_artist a\s
                                WHERE  a.lastfm_id = ?\s
                                       AND a.playnumber > ?) usera\s
                               JOIN (SELECT artist_id AS a2,
                                            a.lastfm_id AS l2,
                                            a.playnumber AS p2
                                     FROM   scrobbled_artist a\s
                                     WHERE  a.lastfm_id= ?
                                            AND a.playnumber > ? ) userb\s
                                 ON usera.a1 = userb.a2;\s""";
        executeComparisonWithThreshold(connection, ownerLastfmID, receiverLastFMId, threshold, queryBody);

    }

    private void executeComparisonWithThreshold(Connection connection, String ownerLastfmID, String receiverLastFMId, long threshold, String queryBody) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            int i = 1;
            preparedStatement.setString(i++, ownerLastfmID);
            preparedStatement.setLong(i++, threshold);

            preparedStatement.setString(i++, receiverLastFMId);
            preparedStatement.setLong(i, threshold);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public Affinity getPercentageStats(Connection connection, String ownerLastfmID, String receiverLastFMId, long threshold) {
        String queryBody = """
                SELECT Count(*) AS matchingcount,
                       ((SELECT Count(*)
                              FROM   scrobbled_artist
                              WHERE  lastfm_id = ?) + (SELECT Count(*)
                                                                    FROM   scrobbled_artist
                                                                    WHERE  lastfm_id = ?))   /2 AS minsize,
                   (SELECT count(*) FROM affinity WHERE abs(p1 - p2 ) < ? * 2) AS closematch,
                   (SELECT count(*) FROM affinity WHERE (p1 > (? * 10)  AND p2 > (? * 5)) OR (p2 > (? * 10) AND  p1 > (? * 5))) AS truematching
                   FROM affinity
                """;
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            int i = 1;
            preparedStatement.setString(i++, ownerLastfmID);
            preparedStatement.setString(i++, receiverLastFMId);
            preparedStatement.setLong(i++, threshold);
            preparedStatement.setLong(i++, threshold);
            preparedStatement.setLong(i++, threshold);
            preparedStatement.setLong(i++, threshold);
            preparedStatement.setLong(i, threshold);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                long matchingCount = resultSet.getLong("matchingCount");
                long minSize = resultSet.getLong("minSize");
                long closeMatch = resultSet.getLong("closeMatch");

                long trueMatching = resultSet.getLong("trueMatching");
                return new Affinity(threshold, matchingCount, closeMatch, trueMatching, minSize, ownerLastfmID, receiverLastFMId);
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        throw new ChuuServiceException();

    }

    @Override
    public String[] doRecommendations(Connection connection, String ogLastFmId, String receiverLastFMId) {
        String[] artistRecs = new String[]{null, null};
        String queryBody = """
                SELECT (SELECT\s
                b.name\s
                FROM scrobbled_artist a\s
                JOIN artist b ON a.artist_id = b.id\s
                WHERE a.lastfm_id = ?\s
                AND a.artist_id NOT IN (SELECT in_b.artist_id FROM scrobbled_artist in_b WHERE in_b.lastfm_id = ? ) ORDER BY a.playnumber DESC LIMIT 1) first,
                (SELECT\s
                b1.name\s
                FROM scrobbled_artist a1\s
                JOIN artist b1 ON a1.artist_id = b1.id\s
                WHERE a1.lastfm_id = ? \s
                AND a1.artist_id NOT IN (SELECT in1_b.artist_id FROM scrobbled_artist in1_b WHERE in1_b.lastfm_id = ? ) ORDER BY a1.playnumber DESC LIMIT 1) second""";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            int i = 1;
            preparedStatement.setString(i++, ogLastFmId);
            preparedStatement.setString(i++, receiverLastFMId);
            preparedStatement.setString(i++, receiverLastFMId);
            preparedStatement.setString(i, ogLastFmId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String first = resultSet.getString("first");
                String second = resultSet.getString("second");
                artistRecs[0] = first;
                artistRecs[1] = second;

            }
            return artistRecs;

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public void setServerTempTable(Connection connection, long guildId, String ogLastfmID, long threshold) {
        String queryBody =
                """
                        CREATE TEMPORARY TABLE server_affinity\s
                        SELECT *\s
                        FROM   (SELECT artist_id AS a1,\s
                                       a.lastfm_id AS l1,\s
                                       a.playnumber AS p1
                                FROM   scrobbled_artist a\s
                        JOIN user b  ON a.lastfm_id = b.lastfm_id
                        JOIN user_guild c ON b.discord_id = c.discord_id\s
                        WHERE c.guild_id = ?\s
                        AND b.lastfm_id != ?
                                AND a.playnumber >= ?) server\s
                         JOIN (SELECT artist_id AS a2,
                                            a.lastfm_id AS l2,
                                            a.playnumber AS p2
                                     FROM   scrobbled_artist a\s
                                     WHERE  a.lastfm_id= ?
                                            AND a.playnumber >= ? )\s
                        user ON server.a1 = user.a2\s""";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            int i = 1;
            preparedStatement.setLong(i++, guildId);
            preparedStatement.setString(i++, ogLastfmID);
            preparedStatement.setLong(i++, threshold);
            preparedStatement.setString(i++, ogLastfmID);
            preparedStatement.setLong(i, threshold);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void setGlobalTable(Connection connection, String ogLastfmID, long threshold) {

        String queryBody
                = """
                CREATE TEMPORARY TABLE global_server_affinity\s
                SELECT *\s
                FROM   (SELECT artist_id AS a1,\s
                               a.lastfm_id AS l1,\s
                               a.playnumber AS p1
                        FROM   scrobbled_artist a\s
                JOIN user b  ON a.lastfm_id = b.lastfm_id
                WHERE  b.privacy_mode  in ('TAG','LAST_NAME') AND b.lastfm_id != ?
                        AND a.playnumber >= ?) server\s
                 JOIN (SELECT artist_id AS a2,
                                    a.lastfm_id AS l2,
                                    a.playnumber AS p2
                             FROM   scrobbled_artist a\s
                             WHERE  a.lastfm_id= ?
                                    AND a.playnumber >= ? )\s
                user ON server.a1 = user.a2\s""";
        executeComparisonWithThreshold(connection, ogLastfmID, ogLastfmID, threshold, queryBody);

    }

    @Override
    public List<ArtistLbGlobalEntry> getGlobalMatchingCount(Connection connection) {
        List<ArtistLbGlobalEntry> affinityList = new ArrayList<>();
        String queryBody = """
                SELECT u.discord_id ,l1 AS lastfmid,Count(*) AS matchingcount,u.privacy_mode \s
                   FROM global_server_affinity main       JOIN user u ON main.l1 = u.lastfm_id
                   GROUP BY main.l1,u.lastfm_id order by matchingcount desc  limit 50""";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String lastfmid = resultSet.getString("lastfmid");
                long discordId = resultSet.getLong("discord_id");
                long matchingCount = resultSet.getLong("matchingcount");
                PrivacyMode privacyMode = PrivacyMode.valueOf(resultSet.getString("privacy_mode"));


                ArtistLbGlobalEntry artistLbEntry = new ArtistLbGlobalEntry(lastfmid, discordId, Math.toIntExact(matchingCount), privacyMode);
                affinityList.add(artistLbEntry);
            }
            return affinityList;

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public List<LbEntry> getMatchingCount(Connection connection) {
        List<LbEntry> affinityList = new ArrayList<>();
        String queryBody = """
                SELECT u.discord_id ,l1 AS lastfmid,Count(*) AS matchingcount \s
                   FROM server_affinity main       JOIN user u ON main.l1 = u.lastfm_id
                   GROUP BY main.l1,u.lastfm_id""";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String lastfmid = resultSet.getString("lastfmid");
                long discordId = resultSet.getLong("discord_id");
                long matchingCount = resultSet.getLong("matchingcount");

                ArtistLbEntry artistLbEntry = new ArtistLbEntry(lastfmid, discordId, Math.toIntExact(matchingCount));
                affinityList.add(artistLbEntry);
            }
            return affinityList;

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<GlobalAffinity> doGlobalAffinity(Connection connection, String ogLastfmId, long threshold) {
        List<GlobalAffinity> affinityList = new ArrayList<>();
        String queryBody = """
                SELECT u.discord_id ,u.privacy_mode,l1 AS lastfmid,Count(*) AS matchingcount,\s
                       ((SELECT Count(*)\s
                              FROM   scrobbled_artist\s
                              WHERE  lastfm_id = main.l1) + (SELECT Count(*)\s
                                                                    FROM   scrobbled_artist\s
                                                                    WHERE  lastfm_id = ?)) /2 AS minsize ,


                (SELECT count(*) FROM global_server_affinity t1 WHERE abs(p1 - p2 ) < ? * 2 AND t1.l1 = main.l1
                ) AS closematch,
                    (SELECT count(*)\s
                   FROM global_server_affinity t2
                   WHERE t2.l1 = main.l1 AND\s
                   (
                   (p1 > (? * 10) AND p2 > (? * 5)) OR\s
                   (p2 > (? * 10) AND  p1 > (? * 5)))\s
                   ) AS truematching
                   FROM global_server_affinity main       JOIN user u ON main.l1 = u.lastfm_id
                   GROUP BY main.l1,u.lastfm_id order by truematching limit 100""";
        // Limit but order by what
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            int i = 1;
            preparedStatement.setString(i++, ogLastfmId);
            preparedStatement.setLong(i++, threshold);
            preparedStatement.setLong(i++, threshold);
            preparedStatement.setLong(i++, threshold);
            preparedStatement.setLong(i++, threshold);
            preparedStatement.setLong(i, threshold);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String lastfmid = resultSet.getString("lastfmid");
                long discordId = resultSet.getLong("discord_id");
                long matchingCount = resultSet.getLong("matchingcount");

                long minSize = resultSet.getLong("minsize");
                long closeMatch = resultSet.getLong("closematch");

                long trueMatching = resultSet.getLong("trueMatching");
                PrivacyMode privacyMode = PrivacyMode.valueOf(resultSet.getString("privacy_mode"));

                GlobalAffinity affinity = new GlobalAffinity(threshold, matchingCount, closeMatch, trueMatching, minSize, ogLastfmId, lastfmid, privacyMode);
                affinity.setDiscordId(discordId);
                affinityList.add(affinity);
            }
            return affinityList;

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<Affinity> doServerAffinity(Connection connection, String ogLastfmId, long threshold) {
        List<Affinity> affinityList = new ArrayList<>();
        String queryBody = """
                SELECT u.discord_id ,l1 AS lastfmid,Count(*) AS matchingcount,\s
                       ((SELECT Count(*)\s
                              FROM   scrobbled_artist\s
                              WHERE  lastfm_id = main.l1) +  (SELECT Count(*)\s
                                                                    FROM   scrobbled_artist\s
                                                                    WHERE  lastfm_id = ?)) /2 AS minsize ,


                (SELECT count(*) FROM server_affinity t1 WHERE abs(p1 - p2 ) < ? * 2 AND t1.l1 = main.l1
                ) AS closematch,
                    (SELECT count(*)\s
                   FROM server_affinity t2
                   WHERE t2.l1 = main.l1 AND\s
                   (
                   (p1 > (? * 10) AND p2 > (? * 5)) OR\s
                   (p2 > (? * 10) AND  p1 > (? * 5)))\s
                   ) AS truematching
                   FROM server_affinity main       JOIN user u ON main.l1 = u.lastfm_id
                   GROUP BY main.l1,u.lastfm_id""";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            int i = 1;
            preparedStatement.setString(i++, ogLastfmId);
            preparedStatement.setLong(i++, threshold);
            preparedStatement.setLong(i++, threshold);
            preparedStatement.setLong(i++, threshold);
            preparedStatement.setLong(i++, threshold);
            preparedStatement.setLong(i, threshold);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String lastfmid = resultSet.getString("lastfmid");
                long discordId = resultSet.getLong("discord_id");
                long matchingCount = resultSet.getLong("matchingcount");

                long minSize = resultSet.getLong("minsize");
                long closeMatch = resultSet.getLong("closematch");

                long trueMatching = resultSet.getLong("trueMatching");
                Affinity affinity = new Affinity(threshold, matchingCount, closeMatch, trueMatching, minSize, ogLastfmId, lastfmid);
                affinity.setDiscordId(discordId);
                affinityList.add(affinity);
            }
            return affinityList;

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void cleanUp(Connection connection, boolean isServer) {
        String queryBody = "drop table if EXISTS " + (isServer ? "server_affinity" : "affinity");
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void cleanUpGlobal(Connection connection, boolean isServer) {
        String queryBody = "drop table if EXISTS global_server_affinity";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

}
