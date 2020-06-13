package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ChuuService;
import dao.entities.GlobalCrown;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GlobalArtistCommand extends ConcurrentCommand<ArtistParameters> {
    private final DiscogsApi discogsApi;
    private final Spotify spotify;

    public GlobalArtistCommand(ChuuService dao) {
        super(dao);
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        this.spotify = SpotifySingleton.getInstance();
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.BOT_STATS;
    }

    @Override
    public Parser<ArtistParameters> getParser() {
        ArtistParser parser = new ArtistParser(getService(), lastFM);
        parser.addOptional(new OptionalEntity("--nobotted", "discard users that have been manually flagged as potentially botted accounts"));
        return parser;
    }

    @Override
    public String getDescription() {
        return "An overview of your global ranking of an artist";
    }

    @Override
    public List<String> getAliases() {
        return List.of("global", "g");
    }

    @Override
    public String getName() {
        return "Global Artist Overview";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ArtistParameters returned = parser.parse(e);
        if (returned == null)
            return;
        long userId = returned.getLastFMData().getDiscordId();
        ScrobbledArtist validable = new ScrobbledArtist(returned.getArtist(), 0, "");
        CommandUtil.validate(getService(), validable, lastFM, discogsApi, spotify);
        returned.setScrobbledArtist(validable);
        boolean b = !returned.hasOptional("--nobotted");
        List<GlobalCrown> globalArtistRanking = getService().getGlobalArtistRanking(validable.getArtistId(), b, e.getAuthor().getIdLong());
        String artist = CommandUtil.cleanMarkdownCharacter(validable.getArtist());
        if (globalArtistRanking.isEmpty()) {
            sendMessageQueue(e, "No one knows " + artist);
            return;
        }
        Optional<GlobalCrown> yourPosition = globalArtistRanking.stream().filter(x -> x.getDiscordId() == userId).findFirst();
        int totalPeople = globalArtistRanking.size();
        int totalPlays = globalArtistRanking.stream().mapToInt(GlobalCrown::getPlaycount).sum();
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (yourPosition.isPresent()) {
            GlobalCrown globalCrown = yourPosition.get();
            int position = globalCrown.getRanking();

            embedBuilder.addField("Position:", position + "/" + totalPeople, true);
            //It means we have someone ahead of us
            if (position != 1) {
                if (position == 2) {
                    if (globalArtistRanking.get(0).isBootedAccount()) {
                        embedBuilder.addField("Plays for global crown:", String.valueOf((globalArtistRanking.get(0).getPlaycount() - globalCrown.getPlaycount() + 1)), true);
                    }
                    embedBuilder.addField("Plays for global crown:", String.valueOf((globalArtistRanking.get(0).getPlaycount() - globalCrown.getPlaycount() + 1)), true);
                    embedBuilder.addField("Your Plays:", String.valueOf(globalCrown.getPlaycount()), true);

                } else {
                    embedBuilder.addField("Plays to rank up:", String.valueOf((globalArtistRanking.get(position - 2).getPlaycount() - globalCrown.getPlaycount() + 1)), true);
                    embedBuilder.addField("Plays for first position:", String.valueOf((globalArtistRanking.get(0).getPlaycount() - globalCrown.getPlaycount() + 1)), true);
                    embedBuilder.addField("Your Plays:", String.valueOf(globalCrown.getPlaycount()), false);
                }

            } else {
                if (globalArtistRanking.size() > 1) {
                    embedBuilder.addField("Ahead of second:", (globalCrown.getPlaycount() - globalArtistRanking.get(1).getPlaycount()) + " plays", true);
                } else {
                    embedBuilder.addBlankField(true);
                }
                embedBuilder.addField("Your Plays:", String.valueOf(globalCrown.getPlaycount()), true);
            }
        } else {
            embedBuilder.addField("Plays for first position:", String.valueOf((globalArtistRanking.get(0).getPlaycount())), false);
        }
        if (e.isFromGuild()) {
            StringBuilder serverStats = new StringBuilder();
            long artistFrequencies = getService().getArtistFrequencies(e.getGuild().getIdLong(), validable.getArtistId());
            serverStats.append(String.format("**%d** listeners%n", artistFrequencies));
            long serverArtistPlays = getService().getServerArtistPlays(e.getGuild().getIdLong(), validable.getArtistId());
            serverStats.append(String.format("**%d** plays%n", serverArtistPlays));
            embedBuilder.
                    addField(String.format("%s's stats", CommandUtil.cleanMarkdownCharacter(e.getGuild().getName())), serverStats.toString(), true);
        }

        String globalStats = String.format("**%d** listeners%n", totalPeople) +
                String.format("**%d** plays%n", totalPlays);
        embedBuilder
                .addField(String.format("%s's stats", CommandUtil.cleanMarkdownCharacter(e.getJDA().getSelfUser().getName())), globalStats, true);

        embedBuilder.setImage(validable.getUrl());
        embedBuilder.setTitle("Who knows " + artist + " globally?");
        messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();

    }

}
