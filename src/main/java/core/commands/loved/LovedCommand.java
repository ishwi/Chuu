package core.commands.loved;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ServiceView;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import dao.entities.ScrobbledTrack;
import dao.entities.TrackWithArtistId;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    protected void onCommand(Context e, @Nonnull ChuuDataParams params) throws LastFmException, InstanceNotFoundException {
        CountWrapper<List<TrackWithArtistId>> wrapper = lastFM.getLovedSongs(params.getLastFMData());
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, params.getLastFMData().getDiscordId());
        if (wrapper.getRows() == 0) {
            sendMessageQueue(e, "%s doesn't have any loved track. Consider using the `%slove` command!".formatted(uInfo.username(), CommandUtil.getMessagePrefix(e)));
            return;
        }


        String userName = params.getLastFMData().getName();
        List<TrackWithArtistId> songs = wrapper.getResult();
        ZoneId zoneId = params.getLastFMData().getTimeZone().toZoneId();

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setAuthor("%s's loved songs".formatted(uInfo.username()), PrivacyUtils.getLastFmUser(userName) + "/loved", uInfo.urlImage())
                .setFooter("%d total %s loved".formatted(wrapper.getRows(), CommandUtil.singlePlural(wrapper.getRows(), "song", "songs")));

        new PaginatorBuilder<>(e, embedBuilder, songs).mapper(t -> "**[%s - %s](%s)** - %s\n".formatted(t.getName(), t.getArtist(), PrivacyUtils.getLastFmArtistTrackUserUrl(t.getArtist(), t.getName(), userName), CommandUtil.getDateTimestampt(Instant.ofEpochSecond(t.getUtc()))))
                .unnumered().build().queue();


        CompletableFuture.runAsync(() -> db.updateLovedSongs(params.getLastFMData().getName(), wrapper.getResult().stream().map(w -> new ScrobbledTrack(w.getArtist(), w.getName(), 0, true, 0, null, null, null)).toList()));

    }
}
