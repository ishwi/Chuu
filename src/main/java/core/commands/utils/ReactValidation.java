package core.commands.utils;

import core.Chuu;
import core.commands.Context;
import core.parsers.params.EmotiParameters;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReactValidation {


    public static List<String> validateEmotes(Context e, @Nonnull EmotiParameters params) {
        TextChannel channelToTestIn = Chuu.getShardManager().getTextChannelById(Chuu.channelId);
        List<CustomEmoji> rejected = new ArrayList<>();
        boolean sentMessage = false;

        for (RichCustomEmoji emote : params.getEmotes()) {
            if (channelToTestIn != null && !emote.canInteract(e.getJDA().getSelfUser(), channelToTestIn, true)) {
                RichCustomEmoji emoteById = Chuu.getShardManager().getEmojiById(emote.getId());
                if (emoteById != null) {
                    if (!emoteById.canInteract(e.getJDA().getSelfUser(), channelToTestIn, true)) {
                        rejected.add(emote);
                    }
                } else {
                    rejected.add(emote);
                    if (Chuu.getShardManager().getShardsQueued() > 0 && !sentMessage) {
                        sentMessage = true;
                        e.sendMessage("Bot is still loading, so some emotes might not be available yet").queue();
                    }
                }
            }
        }
        if (!rejected.isEmpty()) {
            e.sendMessage("Couldn't use some emotes because of permissions or unknown emotes.\n" +
                    "The following emotes were ignored: " + rejected.stream().map(Emoji::getFormatted).collect(Collectors.joining(" "))).queue();

        }
        return params.getEmotis().stream().filter(emotable -> {
            if (emotable instanceof EmotiParameters.DiscordEmote emote) {
                return !rejected.contains(emote.entity());
            }
            return true;
        }).map(EmotiParameters.Emotable::getContent).toList();
    }

}
