package core.parsers;

import core.commands.Context;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.ChannelParameters;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;
import java.util.Optional;

public class ChannelParser extends Parser<ChannelParameters> {

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    protected ChannelParameters parseLogic(Context e, String[] words) {

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
    public List<Explanation> getUsages() {
        return List.of(() -> new ExplanationLineType("Channel", "Channel can be either the name of a channel or the id of the channel", OptionType.CHANNEL));
    }


}
