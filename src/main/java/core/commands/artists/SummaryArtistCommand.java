package core.commands.artists;

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
import core.services.validators.ArtistValidator;
import core.util.ServiceView;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SummaryArtistCommand extends ConcurrentCommand<ArtistParameters> {

    private final MusicBrainzService mb;

    public SummaryArtistCommand(ServiceView dao) {
        super(dao);
        this.mb = MusicBrainzServiceSingleton.getInstance();
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
    public void onCommand(Context e, @Nonnull ArtistParameters params) throws LastFmException {

        final ScrobbledArtist sA = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), !params.isNoredirect());
        LastFMData data = params.getLastFMData();
        long whom = data.getDiscordId();
        ArtistSummary summary = lastFM.getArtistSummary(sA.getArtist(), data);
        ArtistMusicBrainzDetails artistDetails = mb.getArtistDetails(new ArtistInfo(null, summary.artistname(), summary.mbid()));
        long globalArtistPlays = db.getGlobalArtistPlays(sA.getArtistId());
        long globalArtistFrequencies = db.getGlobalArtistFrequencies(sA.getArtistId());

        String username = getUserString(e, whom, data.getName());
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e);
        List<String> tags = new TagCleaner(db).cleanTags(summary.tags());
        String tagsField = tags.isEmpty()
                ? ""
                : tags.stream()
                .map(tag -> "[" + CommandUtil.escapeMarkdown(tag) + "](" + LinkUtils.getLastFmTagUrl(tag) + ")")
                .collect(Collectors.joining(" - "));

        String similarField =
                summary.similars().isEmpty()
                        ? ""
                        : summary.similars().stream()
                        .map(art -> "[" + CommandUtil.escapeMarkdown(art) + "](" + LinkUtils.getLastFmArtistUrl(art) + ")")
                        .collect(Collectors.joining(" - "));

        embedBuilder.setTitle("Information about " + CommandUtil.escapeMarkdown(summary.artistname()), LinkUtils.getLastFmArtistUrl(sA.getArtist()));

        if (e.isFromGuild()) {
            StringBuilder serverStats = new StringBuilder();
            long artistFrequencies = db.getArtistFrequencies(e.getGuild().getIdLong(), sA.getArtistId());
            serverStats.append(String.format("**%d** listeners%n", artistFrequencies));
            long serverArtistPlays = db.getServerArtistPlays(e.getGuild().getIdLong(), sA.getArtistId());
            serverStats.append(String.format("**%d** plays%n", serverArtistPlays));
            embedBuilder.
                    addField(String.format("%s's stats", CommandUtil.escapeMarkdown(e.getGuild().getName())), serverStats.toString(), true);
        }
        String lastFMStats = String.format("**%d** listeners%n", summary.listeners()) +
                             String.format("**%d** plays%n", summary.playcount());
        String globalStats = String.format("**%d** listeners%n", globalArtistFrequencies) +
                             String.format("**%d** plays%n", globalArtistPlays);
        embedBuilder
                .addField(String.format("%s's stats", CommandUtil.escapeMarkdown(e.getJDA().getSelfUser().getName())), globalStats, true)
                .addField("Last.FM stats", lastFMStats, true)
                .addField(username + "'s plays:", "**" + summary.userPlayCount() + "** plays", false);
        if (artistDetails != null) {
            if (artistDetails.gender() != null) {
                embedBuilder.addField("Gender:", artistDetails.gender(), true);
                if (artistDetails.countryCode() != null)
                    embedBuilder.addBlankField(true);

            }
            if (artistDetails.countryCode() != null) {
                embedBuilder.addField("Country:", ":flag_" + artistDetails.countryCode().toLowerCase() + ":", true);
            }
        }

        embedBuilder.addField("Tags:", tagsField, false)
                .addField("Similars:", similarField, false)
                .addField("Bio:", CommandUtil.escapeMarkdown(summary.summary()), false)
                .setImage(sA.getUrl());

        e.sendMessage(embedBuilder.build()).queue();
        if (!tags.isEmpty()) {
            CommandUtil.runLog(new TagArtistService(db, lastFM, tags, new ArtistInfo(sA.getUrl(), summary.artistname(), summary.mbid())));
        }
    }
}
