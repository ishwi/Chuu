package core.services;

import core.commands.utils.CommandUtil;
import dao.ChuuService;
import dao.entities.EmbedColor;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ColorService {
    private static Set<Long> serversByRole;
    private static Set<Long> usersByRole;
    private static Map<Long, Color[]> usersByColor;
    private static Map<Long, Color[]> serversByColor;

    public static void init(ChuuService chuuService) {
        serversByColor = chuuService.getServerWithPalette();
        usersByColor = chuuService.getUsersWithPalette();
        serversByRole = chuuService.getServerWithColorRole();
        usersByRole = chuuService.getUserWithColorRole();
    }

    public static Color computeColor(MessageReceivedEvent event) {
        long idLong;
        if (event.isFromGuild()) {
            idLong = event.getGuild().getIdLong();
            if (serversByRole.contains(idLong) || (usersByRole.contains(event.getAuthor().getIdLong()) && !serversByColor.containsKey(event.getGuild().getIdLong()))) {
                return Optional.ofNullable(event.getMember())
                        .flatMap(t -> t.getRoles().stream().findFirst())
                        .map(Role::getColor)
                        .orElseGet(CommandUtil::pastelColor);
            }

        } else {
            idLong = -1;
        }
        Color[] pallete = serversByColor.getOrDefault(idLong, usersByColor.getOrDefault(event.getAuthor().getIdLong(), new Color[]{CommandUtil.pastelColor()}));
        return pallete[CommandUtil.rand.nextInt(pallete.length)];
    }

    public static void handleServerChange(long guildId, EmbedColor newEmbedColor) {
        handleLists(guildId, newEmbedColor, serversByRole, serversByColor);
    }

    public static void handleUserChange(long userId, EmbedColor newEmbedColor) {
        handleLists(userId, newEmbedColor, usersByRole, usersByColor);
    }

    private static void handleLists(long userId, EmbedColor newEmbedColor, Set<Long> usersByRole, Map<Long, Color[]> usersByColor) {
        EmbedColor.EmbedColorType type = newEmbedColor.type();
        switch (type) {
            case RANDOM -> usersByRole.remove(userId);
            case ROLE -> {
                usersByColor.remove(userId);
                usersByRole.add(userId);
            }
            case COLOURS -> {
                usersByRole.remove(userId);
                usersByColor.put(userId, newEmbedColor.mapList());
            }
        }
    }


}
