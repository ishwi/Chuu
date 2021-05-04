package core.commands.utils;

import core.Chuu;
import core.commands.Context;
import core.parsers.params.EmotiParameters;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReactValidation {


    public static List<String> validateEmotes(Context e, @NotNull EmotiParameters params) {
        TextChannel channelToTestIn = Chuu.getShardManager().getTextChannelById(Chuu.channelId);
        List<Emote> rejected = new ArrayList<>();
        boolean sentMessage = false;
        for (Emote emote : params.getEmotes()) {
            if (emote.getGuild() != null) {
                if (!emote.canInteract(e.getJDA().getSelfUser(), channelToTestIn, true)) {
                    rejected.add(emote);
                }
            } else {
                Emote emoteById = Chuu.getShardManager().getEmoteById(emote.getId());
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
                          "The following emotes were ignored: " + rejected.stream().map(Emote::getAsMention).collect(Collectors.joining(" "))).queue();

        }
        return params.getEmotis().stream().filter(emotable -> {
            if (emotable instanceof EmotiParameters.CustomEmote emote) {
                return !rejected.contains(emote.entity());
            }
            return true;
        }).map(EmotiParameters.Emotable::getContent).toList();
    }

}
