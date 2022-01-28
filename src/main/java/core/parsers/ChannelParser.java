package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.ChannelParameters;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;
import java.util.Optional;

public class ChannelParser extends Parser<ChannelParameters> {

    public static Optional<GuildChannel> parseChannel(String input, Guild guild) {
        var snowflake = ParserAux.parseSnowflake(input);
        return snowflake.map(value -> guild.getGuildChannelById(value.id()))
                .or(() -> guild.getChannels().stream().filter(x -> x.getName().equalsIgnoreCase(input)).findFirst());
    }

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    public ChannelParameters parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws LastFmException, InstanceNotFoundException {
        return new ChannelParameters(ctx, ctx.e().getOption("channel").getAsGuildChannel());
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
        Optional<GuildChannel> guildChannel = parseChannel(join, e.getGuild());
        if (guildChannel.isEmpty()) {
            sendError("Couldn't find a channel", e);
            return null;
        }
        return new ChannelParameters(e, guildChannel.get());

    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(InteractionAux.required(() -> new ExplanationLineType("channel", "Channel can be either the name of a channel or the id of the channel", OptionType.CHANNEL)));
    }


}
