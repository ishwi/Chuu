package core.commands.loved;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ServiceView;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.TrackWithArtistId;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

public class LovedCommand extends ConcurrentCommand<ChuuDataParams> {
    public LovedCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.LOVE;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db);
    }

    @Override
    public String getDescription() {
        return "A list of all your loved songs";
    }

    @Override
    public List<String> getAliases() {
        return List.of("loved");
    }

    @Override
    public String getName() {
        return "Loved";
    }

    @Override
    protected void onCommand(Context e, @NotNull ChuuDataParams params) throws LastFmException, InstanceNotFoundException {
        CountWrapper<List<TrackWithArtistId>> wrapper = lastFM.getLovedSongs(params.getLastFMData());
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, params.getLastFMData().getDiscordId());
        if (wrapper.getRows() == 0) {
            sendMessageQueue(e, "%s doesn't have any loved track. Consider using the`%slove` command!".formatted(uInfo.getUsername(), CommandUtil.getMessagePrefix(e)));
        }
        String userName = params.getLastFMData().getName();
        List<TrackWithArtistId> songs = wrapper.getResult();
        ZoneId zoneId = params.getLastFMData().getTimeZone().toZoneId();

        List<String> lines = songs.stream().map(t -> "**[%s - %s](%s)** - %s\n".formatted(t.getName(), t.getArtist(), PrivacyUtils.getLastFmArtistTrackUserUrl(t.getArtist(), t.getName(), userName), CommandUtil.getAmericanizedDate(OffsetDateTime.ofInstant(Instant.ofEpochSecond(t.getUtc()), zoneId)))).toList();
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < lines.size() && i < 10; i++) {
            a.append(lines.get(i));
        }
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setAuthor("%s's loved songs".formatted(uInfo.getUsername()), PrivacyUtils.getLastFmUser(userName) + "/loved", uInfo.getUrlImage())
                .setDescription(a)
                .setFooter("%d total %s loved".formatted(wrapper.getRows(), CommandUtil.singlePlural(wrapper.getRows(), "song", "songs")));
        e.sendMessage(embedBuilder.build()).queue(message ->
                new Reactionary<>(lines, message, embedBuilder, false));


    }
}
