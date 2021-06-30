package core.commands.loved;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.*;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ServiceView;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ArtistLovedCommand extends ConcurrentCommand<ArtistParameters> {
    public ArtistLovedCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.LOVE;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "A list of all your loved songs";
    }

    @Override
    public List<String> getAliases() {
        return List.of("artistloved", "aloved");
    }

    @Override
    public String getName() {
        return "Artist loved";
    }

    @Override
    public String slashName() {
        return "loved-artist";
    }

    @Override
    protected void onCommand(Context e, @NotNull ArtistParameters params) throws LastFmException, InstanceNotFoundException {
        CountWrapper<List<TrackWithArtistId>> wrapper = lastFM.getLovedSongs(params.getLastFMData());
        ScrobbledArtist sA = CommandUtil.onlyCorrection(db, params.getArtist(), lastFM, !params.isNoredirect());
        List<TrackWithArtistId> artists = wrapper.getResult().stream().filter(w -> w.getArtist().equalsIgnoreCase(sA.getArtist())).toList();
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, params.getLastFMData().getDiscordId());
        int size = artists.size();
        if (size == 0) {
            sendMessageQueue(e, "%s doesn't have any **%s** loved track. Consider using the `%slove` command!".formatted(uInfo.getUsername(),
                    sA.getArtist(),
                    CommandUtil.getMessagePrefix(e)));
            return;
        }


        String userName = params.getLastFMData().getName();
        ZoneId zoneId = params.getLastFMData().getTimeZone().toZoneId();

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor("%s's %s loved songs".formatted(uInfo.getUsername(), sA.getArtist()), PrivacyUtils.getLastFmUser(userName) + "/loved", uInfo.getUrlImage())
                .setFooter("%d total %s %s loved".formatted(size, sA.getArtist(), CommandUtil.singlePlural(size, "song", "songs")));

        new ListSender<>(e,
                artists,
                t -> "**[%s](%s)** - %s\n".formatted(t.getName(), PrivacyUtils.getLastFmArtistTrackUserUrl(t.getArtist(), t.getName(), userName), CommandUtil.getDateTimestampt(Instant.ofEpochSecond(t.getUtc()))),
                embedBuilder)
                .doSend(false);

        CompletableFuture.runAsync(() -> db.updateLovedSongs(params.getLastFMData().getName(), wrapper.getResult().stream().map(w -> new ScrobbledTrack(w.getArtist(), w.getName(), 0, true, 0, null, null, null)).toList()));

    }
}
