package core.parsers;

import core.exceptions.LastFmException;
import core.parsers.params.ChannelParameters;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Optional;
import java.util.regex.Pattern;

public class ChannelParser extends Parser<ChannelParameters> {
    private static final Pattern idParser = Pattern.compile("^(?:<(?:@!?|@&|#)(?<sid>[0-9]{17,21})>|(?<id>[0-9]{17,21}))$");

    @Override
    protected void setUpErrorMessages() {

    }

    @Override
    protected ChannelParameters parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {

        if (words.length == 0) {
            Member member = e.getMember();
            if (member != null && member.getVoiceState() != null && member.getVoiceState().getChannel() != null) {
                return new ChannelParameters(e, member.getVoiceState().getChannel());
            }
        }
        String join = String.join(" ", words);
        var snowflake = parseSnowflake(join);
        Optional<GuildChannel> guildChannel = snowflake.map(value -> e.getGuild().getGuildChannelById(value.id))
                .or(() -> e.getGuild().getChannels().stream().filter(x -> x.getName().equalsIgnoreCase(join)).findFirst());
        if (guildChannel.isEmpty()) {
            sendError("Couldn't find a channel", e);
            return null;
        }
        return new ChannelParameters(e, guildChannel.get());

    }

    private Optional<Snowflake> parseSnowflake(String words) {
        var match = idParser.matcher(words);

        if (match.matches()) {
            String sid = match.group("sid");
            if (sid == null) {
                sid = match.group("id");
            }
            Snowflake value = new Snowflake(Long.parseLong(sid));
            return Optional.of(value);
        }
        return Optional.empty();
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + "**channel_name|channel_id**";
    }

    record Snowflake(long id) {
    }
}
