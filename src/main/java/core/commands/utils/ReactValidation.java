package core.commands.utils;

import core.Chuu;
import core.parsers.params.EmotiParameters;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReactValidation {


    public static List<String> validateEmotes(MessageReceivedEvent e, @NotNull EmotiParameters params) {
        TextChannel channelToTestIn = Chuu.getShardManager().getTextChannelById(Chuu.channelId);
        List<Emote> rejected = new ArrayList<>();
        for (Emote emote : params.getEmotes()) {
            if (!emote.canInteract(e.getJDA().getSelfUser(), channelToTestIn, true)) {
                rejected.add(emote);
            }
        }
        if (!rejected.isEmpty()) {
            e.getChannel().sendMessage("Couldn't use some emotes because of permissions or unknown emotes.\n" +
                    "The following emotes were ignored: " + rejected.stream().map(Emote::getAsMention).collect(Collectors.joining(" "))).queue();

        }
        return params.getEmotis().stream().filter(emotable -> {
            if (emotable instanceof EmotiParameters.CustomEmote emote) {
                return !rejected.contains(emote.entity());
            }
            return true;
        }).map(EmotiParameters.Emotable::getContent).collect(Collectors.toList());
    }
}
