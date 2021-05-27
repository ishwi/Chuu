package dao;

import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import java.util.*;

interface UserGuildDao {

    void createGuild(Connection con, long guildId);

    void insertUserData(Connection con, LastFMData lastFMData);

    void insertTempUser(Connection con, long discordId, String token);

    LastFMData findLastFmData(Connection con, long discordId) throws dao.exceptions.InstanceNotFoundException;

    List<Long> guildsFromUser(Connection connection, long userId);

    MultiValuedMap<Long, Long> getWholeUserGuild(Connection connection);

    void updateLastFmData(Connection con, LastFMData lastFMData);

    void removeUser(Connection con, Long discordId);

    void removeUserGuild(Connection con, long discordId, long guildId);

    List<UsersWrapper> getAll(Connection connection, long guildId);

    List<UsersWrapper> getAllNotObscurify(Connection connection, long guildId);

    List<UsersWrapper> getAllNonPrivate(Connection connection, long guildId);

    void addGuild(Connection con, long userId, long guildId);

    void addLogo(Connection con, long guildID, BufferedImage image);

    boolean isFlagged(Connection con, String lastfm);

    void removeLogo(Connection connection, long guildId);

    InputStream findLogo(Connection connection, long guildID);

    long getDiscordIdFromLastFm(Connection connection, String lastFmName) throws InstanceNotFoundException;

    long getDiscordIdFromLastFm(Connection connection, String lastFmName, long guildId) throws InstanceNotFoundException;


    LastFMData findByLastFMId(Connection connection, String lastFmID) throws InstanceNotFoundException;


    List<UsersWrapper> getAll(Connection connection);

    void removeRateLimit(Connection connection, long discordId);

    void upsertRateLimit(Connection connection, long discordId, float queriesPerSecond);

    void insertServerDisabled(Connection connection, long discordId, String commandName);

    void insertChannelCommandStatus(Connection connection, long discordId, long channelId, String commandName, boolean enabled);

    void deleteChannelCommandStatus(Connection connection, long discordId, long channelId, String commandName);

    void deleteServerCommandStatus(Connection connection, long discordId, String commandName);


    MultiValuedMap<Long, String> initServerCommandStatuses(Connection connection);

    MultiValuedMap<Pair<Long, Long>, String> initServerChannelsCommandStatuses(Connection connection, boolean enabled);

    void setUserProperty(Connection connection, long discordId, String additional_embed, boolean chartEmbed);

    void setUserProperty(Connection connection, long discordId, String additional_embed, String value);

    void setUserProperty(Connection connection, long discordId, String property, Integer value);

    void setGuildProperty(Connection connection, long guildId, String property, boolean value);

    void setGuildProperty(Connection connection, long guildId, String property, String value);


    <T extends Enum<T>> void setUserProperty(Connection connection, long discordId, String propertyName, T value);


    GuildProperties getGuild(Connection connection, long discordId) throws InstanceNotFoundException;

    LastFMData findLastFmData(Connection connection, long discordID, long guildId) throws InstanceNotFoundException;

    <T extends Enum<T>> void setGuildProperty(Connection connection, long discordId, String propertyName, @Nullable T value);

    void setChartDefaults(Connection connection, long discordId, int x, int y);

    void serverBlock(Connection connection, long discordId, long guildId);

    boolean isUserServerBanned(Connection connection, long userId, long guildID);

    void serverUnblock(Connection connection, long discordId, long guildId);

    long getNPRaw(Connection connection, long discordId);

    void setNpRaw(Connection connection, long discordId, long raw);

    void setChartOptionsRaw(Connection connection, long discordId, long raw);

    long getServerNPRaw(Connection connection, long guildId);

    void setServerNpRaw(Connection connection, long guild_id, long raw);

    void setTimezoneUser(Connection connection, TimeZone timeZone, long idLong);

    TimeZone getTimezone(Connection connection, long userId);

    Set<Long> getGuildsWithDeletableMessages(Connection connection);

    Set<Long> getGuildsWithCoversOn(Connection connection);

    Set<Long> getGuildsWithEmptyColorOverride(Connection connection);

    Set<Long> getGuildsDontRespondOnErrros(Connection connection);


    void changeDiscordId(Connection connection, long userId, String lastFmID);

    CommandStats getCommandStats(long discordId, Connection connection);

    Set<LastFMData> findScrobbleableUsers(Connection connection, long guildId);

    void insertServerReactions(Connection connection, long guildId, List<String> reactions);

    void insertUserReactions(Connection connection, long userId, List<String> reactions);

    void clearServerReactions(Connection connection, long guildId);

    void clearUserReacts(Connection connection, long userId);

    List<String> getServerReactions(Connection connection, long guildId);

    List<String> getUserReacts(Connection connection, long userId);

    Map<Long, Color[]> getServerWithPalette(Connection connection);

    Map<Long, Color[]> getUsersWithPalette(Connection connection);

    Map<Long, EmbedColor.EmbedColorType> getUserColorTypes(Connection connection);

    Map<Long, EmbedColor.EmbedColorType> getServerColorTypes(Connection connection);

    void flagBottedDB(String lastfmId, Connection connection);

    void flagBotted(String lastfmId, Connection connection);

    void insertBannedCover(Connection connection, long albumId, String cover);

    void removeBannedCover(Connection connection, long albumId, String cover);

    void insertServerCustomUrl(Connection connection, long altId, long guildId, long artistId);

    List<LastFMData> getAllData(Connection connection, long guildId);

    Set<Long> getServerBlocked(Connection connection, long guildId);

    List<RoleColour> getRoles(Connection connection, long guildId);

    void addRole(Connection connection, long guildId, int first, int second, String rest, Color color, long roleId);

    VoiceAnnouncement getGuildVoiceAnnouncement(Connection connection, long guildId);

    void setGuildVoiceAnnouncement(Connection connection, long guildId, VoiceAnnouncement voiceAnnouncement);


    void insertObscurity(Connection connection, String lastfmId, double obscurity);

    ServerStats getServerStats(Connection connection, long guildId);

    OptionalDouble obtainObscurity(Connection connection, String lastfmId);
}
