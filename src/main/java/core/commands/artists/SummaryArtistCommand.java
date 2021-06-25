package core.commands.artists;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.services.tags.TagArtistService;
import core.services.tags.TagCleaner;
import dao.ServiceView;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SummaryArtistCommand extends ConcurrentCommand<ArtistParameters> {

    private final Spotify spotify;
    private final DiscogsApi discogsApi;
    private final MusicBrainzService mb;

    public SummaryArtistCommand(ServiceView dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.mb = MusicBrainzServiceSingleton.getInstance();
        this.spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.INFO;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Brief summary of an artist";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("artistinfo", "ai");
    }

    @Override
    public String getName() {
        return "Artist Info";
    }

    @Override
    protected void onCommand(Context e, @NotNull ArtistParameters params) throws LastFmException {

        final ScrobbledArtist scrobbledArtist = new ScrobbledArtist(params.getArtist(), 0, null);
        CommandUtil.validate(db, scrobbledArtist, lastFM, discogsApi, spotify);
        LastFMData data = params.getLastFMData();
        long whom = data.getDiscordId();
        ArtistSummary summary = lastFM.getArtistSummary(scrobbledArtist.getArtist(), data);
        ArtistMusicBrainzDetails artistDetails = mb.getArtistDetails(new ArtistInfo(null, summary.getArtistname(), summary.getMbid()));
        long globalArtistPlays = db.getGlobalArtistPlays(scrobbledArtist.getArtistId());
        long globalArtistFrequencies = db.getGlobalArtistFrequencies(scrobbledArtist.getArtistId());

        String username = getUserString(e, whom, data.getName());
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e);
        List<String> tags = new TagCleaner(db).cleanTags(summary.getTags());
        String tagsField = tags.isEmpty()
                           ? ""
                           : tags.stream()
                                   .map(tag -> "[" + CommandUtil.escapeMarkdown(tag) + "](" + LinkUtils.getLastFmTagUrl(tag) + ")")
                                   .collect(Collectors.joining(" - "));

        String similarField =
                summary.getSimilars().isEmpty()
                ? ""
                : summary.getSimilars().stream()
                        .map(art -> "[" + CommandUtil.escapeMarkdown(art) + "](" + LinkUtils.getLastFmArtistUrl(art) + ")")
                        .collect(Collectors.joining(" - "));

        embedBuilder.setTitle("Information about " + CommandUtil.escapeMarkdown(summary.getArtistname()), LinkUtils.getLastFmArtistUrl(scrobbledArtist.getArtist()));

        if (e.isFromGuild()) {
            StringBuilder serverStats = new StringBuilder();
            long artistFrequencies = db.getArtistFrequencies(e.getGuild().getIdLong(), scrobbledArtist.getArtistId());
            serverStats.append(String.format("**%d** listeners%n", artistFrequencies));
            long serverArtistPlays = db.getServerArtistPlays(e.getGuild().getIdLong(), scrobbledArtist.getArtistId());
            serverStats.append(String.format("**%d** plays%n", serverArtistPlays));
            embedBuilder.
                    addField(String.format("%s's stats", CommandUtil.escapeMarkdown(e.getGuild().getName())), serverStats.toString(), true);
        }
        String lastFMStats = String.format("**%d** listeners%n", summary.getListeners()) +
                             String.format("**%d** plays%n", summary.getPlaycount());
        String globalStats = String.format("**%d** listeners%n", globalArtistFrequencies) +
                             String.format("**%d** plays%n", globalArtistPlays);
        embedBuilder
                .addField(String.format("%s's stats", CommandUtil.escapeMarkdown(e.getJDA().getSelfUser().getName())), globalStats, true)
                .addField("Last.FM stats", lastFMStats, true)
                .addField(username + "'s plays:", "**" + summary.getUserPlayCount() + "** plays", false);
        if (artistDetails != null) {
            if (artistDetails.getGender() != null) {
                embedBuilder.addField("Gender:", artistDetails.getGender(), true);
                if (artistDetails.getCountryCode() != null)
                    embedBuilder.addBlankField(true);

            }
            if (artistDetails.getCountryCode() != null) {
                embedBuilder.addField("Country:", ":flag_" + artistDetails.getCountryCode().toLowerCase() + ":", true);
            }
        }

        embedBuilder.addField("Tags:", tagsField, false)
                .addField("Similars:", similarField, false)
                .addField("Bio:", CommandUtil.escapeMarkdown(summary.getSummary()), false)
                .setImage(scrobbledArtist.getUrl());

        e.sendMessage(embedBuilder.build()).queue();
        if (!tags.isEmpty()) {
            executor.submit(new TagArtistService(db, lastFM, tags, new ArtistInfo(scrobbledArtist.getUrl(), summary.getArtistname(), summary.getMbid())));
        }
    }
}
