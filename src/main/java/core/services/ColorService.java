package core.services;

import core.Chuu;
import core.commands.Context;
import core.commands.utils.CommandUtil;
import dao.ChuuService;
import dao.entities.EmbedColor;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ColorService {

    private static Map<Long, EmbedColor.EmbedColorType> guildColorTypes;
    private static Map<Long, EmbedColor.EmbedColorType> userColorTypes;
    private static Map<Long, Color[]> usersByColor;
    private static Map<Long, Color[]> guildByColors;
    private static Set<Long> hasEmptyGuild;

    public static void init(ChuuService chuuService) {
        guildByColors = chuuService.getServerWithPalette();
        usersByColor = chuuService.getUsersWithPalette();
        guildColorTypes = chuuService.getServerColorTypes();
        userColorTypes = chuuService.getUserColorTypes();
        hasEmptyGuild = chuuService.getGuildsWithEmptyColorOverride();
    }

    public static @NotNull Color computeColor(Context event) {
        long idLong;
        if (event.isFromGuild()) {
            EmbedColor.EmbedColorType colorType = guildColorTypes.get(event.getGuild().getIdLong());
            if (colorType == null) {
                Color userMode = getUserMode(event);
                if (userMode == null) {
                    return CommandUtil.pastelColor();
                }
            } else {
                if (!hasEmptyGuild.contains(event.getGuild().getIdLong())) {
                    return getServerMode(event, colorType);
                }
                Color userMode = getUserMode(event);
                return Objects.requireNonNullElseGet(userMode,
                        () -> getServerMode(event, colorType));
            }
        }
        Color userMode = getUserMode(event);
        if (userMode == null) {
            return CommandUtil.pastelColor();
        }
        return userMode;
    }

    private static Color getServerMode(Context event, EmbedColor.EmbedColorType colorType) {
        Color color = getColor(event, colorType, guildByColors, event.getGuild().getIdLong());
        return color == null ? CommandUtil.pastelColor() : color;
    }

    private static @Nullable Color getUserMode(Context e) {
        var colorType = userColorTypes.get(e.getAuthor().getIdLong());
        if (colorType == null) {
            return null;
        }
        return getColor(e, colorType, usersByColor, e.getAuthor().getIdLong());
    }

    @Nullable
    private static Color getColor(Context e, EmbedColor.EmbedColorType colorType, Map<Long, Color[]> map, long key) {
        return switch (colorType) {
            case RANDOM -> CommandUtil.pastelColor();

            case ROLE -> getmemberColour(e);

            case COLOURS -> {
                Color[] byColor = map.get(key);
                if (byColor == null) {
                    Chuu.getLogger().warn("Null colours on {} by {} on {}", map, e.getAuthor(), e.getChannel());
                    yield null;
                }
                yield byColor[CommandUtil.rand.nextInt(byColor.length)];
            }
        };
    }

    private static Color getmemberColour(Context e) {
        return Optional.ofNullable(e.getMember())
                .flatMap(t -> t.getRoles().stream().filter(z -> z.getColor() != null).findFirst())
                .map(Role::getColor)
                .orElse(null);
    }


    public static void handleServerChange(long guildId, @Nullable EmbedColor newEmbedColor) {
        handleLists(guildId, newEmbedColor, guildColorTypes, guildByColors);
    }

    public static void handleUserChange(long userId, @Nullable EmbedColor newEmbedColor) {
        handleLists(userId, newEmbedColor, userColorTypes, usersByColor);
    }

    private static void handleLists(long id, @Nullable EmbedColor newEmbedColor, Map<Long, EmbedColor.EmbedColorType> typeMap, Map<Long, Color[]> colorMap) {
        if (newEmbedColor == null) {
            typeMap.remove(id);
            colorMap.remove(id);
            return;
        }
        EmbedColor.EmbedColorType type = newEmbedColor.type();
        switch (type) {
            case RANDOM -> {
                colorMap.remove(id);
                typeMap.put(id, EmbedColor.EmbedColorType.RANDOM);
            }
            case ROLE -> {
                typeMap.put(id, EmbedColor.EmbedColorType.ROLE);
                colorMap.remove(id);
            }
            case COLOURS -> {
                typeMap.put(id, EmbedColor.EmbedColorType.COLOURS);
                colorMap.put(id, newEmbedColor.mapList());
            }
        }
    }


}
