package core.util.botlists;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

public enum BotLists {
    DISCORD_BOTS_GG("https://discord.bots.gg/api/v1/bots/:id/stats", "discord.bots.gg"),
    TOP_GG("https://top.gg/api/bots/:id/stats", "top.gg"),
    DISCORDBOTLISTS("https://discordbotlist.com/api/v1/bots/:id/stats", "discordbotlists.com"),
    BOTSFORDISCORD("https://botsfordiscord.com/api/bot/:id", "botsfordiscord.com"),
    DISCORDEXTREMELIST("https://api.discordextremelist.xyz/v2/bot/:id/stats", "discordextremelist.xyz"),
    YABL("https://yabl.xyz/api", "yabl.xyz"),
    ;


    public final String url;
    public final String key;

    BotLists(String url, String key) {
        this.url = url;
        this.key = key;
    }

    public String generateObj(int guildCount, int shardCount, int voiceConnections) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        return switch (this) {
            // This one uses camelCase
            case DISCORD_BOTS_GG, YABL -> new ObjectMapper().writeValueAsString(new DefaultObj(guildCount, shardCount));
            case TOP_GG -> mapper.writeValueAsString(new TopGG(guildCount, shardCount));
            case DISCORDEXTREMELIST -> mapper.writeValueAsString(new DefaultObj(guildCount, shardCount));
            case DISCORDBOTLISTS -> mapper.writeValueAsString(new DiscordBotList(guildCount, shardCount, voiceConnections));
            case BOTSFORDISCORD -> mapper.writeValueAsString(new BotsForDiscord(guildCount));
        };
    }

    record DefaultObj(int guildCount, int shardCount) {
    }

    record DiscordBotList(int guilds, int users, int voiceConnections) {
    }

    record TopGG(int serverCount, int shardCount) {
    }

    record BotsForDiscord(int serverCount) {

    }
}
