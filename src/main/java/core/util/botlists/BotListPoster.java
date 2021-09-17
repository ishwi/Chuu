package core.util.botlists;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import core.Chuu;
import core.apis.ClientSingleton;
import dao.exceptions.ChuuServiceException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ContentType;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class BotListPoster {
    private final Map<BotLists, String> tokenMap;

    public BotListPoster() {
        tokenMap = readTokens();
    }

    public static Map<BotLists, String> readTokens() {
        Properties properties = new Properties();
        Map<BotLists, String> ids = new HashMap<>();
        try (InputStream in = Chuu.class.getResourceAsStream("/botlists.properties")) {
            if (in == null) {
                return new HashMap<>();
            }
            properties.load(in);
            Map<String, BotLists> keyMap = Arrays.stream(BotLists.values()).collect(Collectors.toMap(w -> w.key, w -> w));
            properties.forEach((w, y) -> {
                String token;
                if (y != null && !StringUtils.isBlank(token = String.valueOf(y)))
                    ids.put(keyMap.get((String) w), token);
            });
        } catch (IOException e) {
            throw new ChuuServiceException(e);
        }
        return ids;
    }

    public void doPost() {
        HttpClient instance = ClientSingleton.getInstance();
        ShardManager shardManager = Chuu.getShardManager();
        JDA jda = shardManager.getShards().get(0);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        long guildSize = shardManager.getGuildCache().size();
        int shards = shardManager.getShards().size();
        int voiceSize = Chuu.playerRegistry.getSize();
        tokenMap.forEach((bot, token) -> {
            URI uri = URI.create(bot.url.replace(":id", jda.getSelfUser().getId()));
            try {
                String payload = bot.generateObj((int) guildSize, shards, voiceSize);
                HttpRequest.Builder builder = HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.ofString(payload));
                builder.header("Authorization", token);
                builder.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
                CompletableFuture<HttpResponse<String>> response = instance.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString());
                response.handle(handleResponse(bot));
            } catch (JsonProcessingException e) {
                Chuu.getLogger().warn("Error posting to botlist {} ", bot, e);
            }
        });
    }

    @Nonnull
    private BiFunction<HttpResponse<String>, Throwable, Object> handleResponse(BotLists bot) {
        return (stringHttpResponse, throwable) -> {
            if (throwable != null) {
                Chuu.getLogger().warn("Error posting to botlist {} | {}", bot, stringHttpResponse, throwable);
            } else {
                Chuu.getLogger().warn("Posted to botlist {} | {}", bot, stringHttpResponse);
            }
            return null;
        };
    }
}
