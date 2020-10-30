package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.services.TagArtistService;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SummaryArtistCommand extends ConcurrentCommand<ArtistParameters> {

    private final Spotify spotify;
    private final DiscogsApi discogsApi;
    private final MusicBrainzService mb;

    public SummaryArtistCommand(ChuuService dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.mb = MusicBrainzServiceSingleton.getInstance();
        this.spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.INFO;
    }

    @Override
    public Parser<ArtistParameters> getParser() {
        return new ArtistParser(getService(), lastFM);
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ArtistParameters params = parser.parse(e);
        if (params == null)
            return;

        final ScrobbledArtist scrobbledArtist = new ScrobbledArtist(params.getArtist(), 0, null);
        CommandUtil.validate(getService(), scrobbledArtist, lastFM, discogsApi, spotify);
        LastFMData data = params.getLastFMData();
        long whom = data.getDiscordId();
        ArtistSummary summary = lastFM.getArtistSummary(scrobbledArtist.getArtist(), data.getName());
        ArtistMusicBrainzDetails artistDetails = mb.getArtistDetails(new ArtistInfo(null, summary.getArtistname(), summary.getMbid()));
        long globalArtistPlays = getService().getGlobalArtistPlays(scrobbledArtist.getArtistId());
        long globalArtistFrequencies = getService().getGlobalArtistFrequencies(scrobbledArtist.getArtistId());

        String username = getUserString(e, whom, data.getName());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        String tagsField = summary.getTags().isEmpty()
                ? ""
                : summary.getTags().stream()
                .map(tag -> "[" + CommandUtil.cleanMarkdownCharacter(tag) + "](" + LinkUtils.getLastFmTagUrl(tag) + ")")
                .collect(Collectors.joining(" - "));

        String similarField =
                summary.getSimilars().isEmpty()
                        ? ""
                        : summary.getSimilars().stream()
                        .map(art -> "[" + CommandUtil.cleanMarkdownCharacter(art) + "](" + LinkUtils.getLastFmArtistUrl(art) + ")")
                        .collect(Collectors.joining(" - "));

        MessageBuilder messageBuilder = new MessageBuilder();
        embedBuilder.setTitle("Information about " + CommandUtil.cleanMarkdownCharacter(summary.getArtistname()), LinkUtils.getLastFmArtistUrl(scrobbledArtist.getArtist()));

        if (e.isFromGuild()) {
            StringBuilder serverStats = new StringBuilder();
            long artistFrequencies = getService().getArtistFrequencies(e.getGuild().getIdLong(), scrobbledArtist.getArtistId());
            serverStats.append(String.format("**%d** listeners%n", artistFrequencies));
            long serverArtistPlays = getService().getServerArtistPlays(e.getGuild().getIdLong(), scrobbledArtist.getArtistId());
            serverStats.append(String.format("**%d** plays%n", serverArtistPlays));
            embedBuilder.
                    addField(String.format("%s's stats", CommandUtil.cleanMarkdownCharacter(e.getGuild().getName())), serverStats.toString(), true);
        }
        String lastFMStats = String.format("**%d** listeners%n", summary.getListeners()) +
                String.format("**%d** plays%n", summary.getPlaycount());
        String globalStats = String.format("**%d** listeners%n", globalArtistFrequencies) +
                String.format("**%d** plays%n", globalArtistPlays);
        embedBuilder
                .addField(String.format("%s's stats", CommandUtil.cleanMarkdownCharacter(e.getJDA().getSelfUser().getName())), globalStats, true)
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
                .addField("Bio:", CommandUtil.cleanMarkdownCharacter(summary.getSummary()), false)
                .setImage(scrobbledArtist.getUrl())
                .setColor(CommandUtil.randomColor());
        e.getChannel().sendMessage(messageBuilder.setEmbed(embedBuilder.build()).build()).queue();
        if (!summary.getTags().isEmpty()) {
            executor.submit(new TagArtistService(getService(), lastFM, summary.getTags(), new ArtistInfo(scrobbledArtist.getUrl(), summary.getArtistname(), summary.getMbid())));
        }
    }
}
