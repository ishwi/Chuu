package core.parsers;

import core.parsers.params.ChannelParameters;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Optional;

public class ChannelParser extends Parser<ChannelParameters> {

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    protected ChannelParameters parseLogic(MessageReceivedEvent e, String[] words) {

        if (words.length == 0) {
            Member member = e.getMember();
            if (member != null && member.getVoiceState() != null && member.getVoiceState().getChannel() != null) {
                return new ChannelParameters(e, member.getVoiceState().getChannel());
            }
        }
        String join = String.join(" ", words);
        var snowflake = ParserAux.parseSnowflake(join);
        Optional<GuildChannel> guildChannel = snowflake.map(value -> e.getGuild().getGuildChannelById(value.id()))
                .or(() -> e.getGuild().getChannels().stream().filter(x -> x.getName().equalsIgnoreCase(join)).findFirst());
        if (guildChannel.isEmpty()) {
            sendError("Couldn't find a channel", e);
            return null;
        }
        return new ChannelParameters(e, guildChannel.get());

    }


    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " **channel_name|channel_id**";
    }


}
