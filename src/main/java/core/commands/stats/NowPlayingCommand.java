package core.commands.stats;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.abstracts.NpCommand;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.params.NowPlayingParameters;
import core.services.ColorService;
import core.services.NPModeBuilder;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NowPlayingCommand extends NpCommand {
    private final DiscogsApi discogsApi;
    private final Spotify spotifyApi;
    private final MusicBrainzService mb;

    public NowPlayingCommand(ChuuService dao) {
        super(dao);
        discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
        spotifyApi = SpotifySingleton.getInstance();
        mb = MusicBrainzServiceSingleton.getInstance();
    }


    @Override
    public void doSomethingWithArtist(NowPlayingArtist nowPlayingArtist, MessageReceivedEvent e, long discordId, LastFMData user, NowPlayingParameters parameters) {
        StringBuilder a = new StringBuilder();

        // Author fields cant have escaped markdown characters
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoNotStripped(e, discordId);

        String urlHolder = userInformation.getUrlImage();
        String userName = userInformation.getUsername();

        EnumSet<NPMode> npModes = EnumSet.noneOf(NPMode.class);
        if (e.isFromGuild()) {
            npModes = db.getServerNPModes(e.getGuild().getIdLong());
        }

        if (npModes.isEmpty() || npModes.size() == 1 && npModes.contains(NPMode.UNKNOWN)) {
            npModes = db.getNPModes(discordId);
        }

        String title = String.format("%s's %s song:", userName, nowPlayingArtist.isNowPlaying() ? "current" : "last");
        String lastFMName = nowPlayingArtist.getUsername();


        a.append("**").append(CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.getArtistName()))
                .append("** | ").append(CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.getAlbumName())).append("\n");

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(ColorService.computeColor(e))
                .setAuthor(title, CommandUtil.getLastFmUser(lastFMName), urlHolder)
                .setThumbnail(CommandUtil.noImageUrl(nowPlayingArtist.getUrl()))
                .setTitle(CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.getSongName()), LinkUtils.getLastFMArtistTrack(nowPlayingArtist.getArtistName(), nowPlayingArtist.getSongName()))
                .setDescription(a);


        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(nowPlayingArtist.getArtistName(), 0, null);
        try {
            CommandUtil.validate(db, scrobbledArtist, lastFM, discogsApi, spotifyApi);
        } catch (LastFmException ignored) {

        }
        String serverName = e.isFromGuild() ? e.getGuild().getName() : null;
        String[] footerSpaces = new String[NPModeBuilder.getSize()];
        List<String> outputList = new ArrayList<>();
        Arrays.fill(footerSpaces, null);
        if (npModes.contains(NPMode.RANDOM)) {
            List<NPMode> allModes = EnumSet.allOf(NPMode.class).stream().filter(x -> !x.equals(NPMode.UNKNOWN)).filter(x -> !x.equals(NPMode.RANDOM)).collect(Collectors.toList());

            npModes = EnumSet.copyOf(IntStream.range(0, CommandUtil.rand.nextInt(4) + 1).mapToObj(x -> allModes.get(CommandUtil.rand.nextInt(allModes.size()))).collect(Collectors.toSet()));
        }
        NPModeBuilder npModeBuilder = new NPModeBuilder(nowPlayingArtist, e, footerSpaces, discordId, userName, npModes, user, embedBuilder, scrobbledArtist, db, lastFM, serverName, mb, outputList, parameters.getData());
        CompletableFuture<?> completableFutures = npModeBuilder.buildNp();


        try {
            completableFutures.get();
        } catch (InterruptedException | ExecutionException ignored) {

        }
        LongAdder counter = new LongAdder();

        List<String> footerMax = new ArrayList<>(footerSpaces.length);
        for (String x : outputList) {
            if (counter.longValue() + x.length() < MessageEmbed.TEXT_MAX_LENGTH) {
                counter.add(x.length());
                footerMax.add(x);
            } else {
                break;
            }
        }

        String footer = String.join(" • ", footerMax);
        // The first line needs the zws to align up with the rest of the lines
        if (!footer.isBlank() && !footer.startsWith(EmbedBuilder.ZERO_WIDTH_SPACE)) {
            footer = EmbedBuilder.ZERO_WIDTH_SPACE + " • " + footer;
        }

        //
        String url = npModes.contains(NPMode.ARTIST_PIC) && scrobbledArtist.getUrl() != null && !scrobbledArtist.getUrl().isBlank()
                ? scrobbledArtist.getUrl()
                : null;
        if (url != null && footer.isBlank()) {
            footer += EmbedBuilder.ZERO_WIDTH_SPACE;
        }
        embedBuilder.setFooter(footer, url);

        e.getChannel().sendMessage(embedBuilder.build()).queue(message -> {
            List<String> serverReactions;
            if (e.isFromGuild()) {
                GuildProperties guildProperties = null;
                try {
                    guildProperties = db.getGuildProperties(e.getGuild().getIdLong());
                } catch (InstanceNotFoundException ignored) {
                }
                if (guildProperties != null && !guildProperties.allowReactions()) {
                    return;
                }
                OverrideMode overrideMode = guildProperties == null ? OverrideMode.OVERRIDE : guildProperties.overrideReactions();
                serverReactions = db.getServerReactions(e.getGuild().getIdLong());

                if (e.getMember() != null && e.getMember().hasPermission(Permission.MESSAGE_ADD_REACTION)) {
                    switch (overrideMode) {
                        case OVERRIDE -> {
                            if (!serverReactions.isEmpty()) {
                                serverReactions = db.getUserReacts(e.getAuthor().getIdLong());
                            }
                        }
                        case ADD -> serverReactions.addAll(db.getUserReacts(e.getAuthor().getIdLong()));
                        case ADD_END -> {
                            List<String> userReacts = db.getUserReacts(e.getAuthor().getIdLong());
                            userReacts.addAll(serverReactions);
                            serverReactions = userReacts;
                        }
                        case EMPTY -> {
                            List<String> userReacts = db.getUserReacts(e.getAuthor().getIdLong());
                            if (!userReacts.isEmpty()) {
                                serverReactions = userReacts;
                            }
                        }
                    }
                }
            } else {
                serverReactions = db.getUserReacts(e.getAuthor().getIdLong());
            }
            if (!serverReactions.isEmpty()) {
                RestAction.allOf(serverReactions.stream().map(unicode -> message.addReaction(unicode).mapToResult()).collect(Collectors.toList())).queue(results -> {
                });
            }
        });
    }


    @Override
    public String getDescription() {
        return "Returns your last or current playing song";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("np", "fm");
    }

    @Override
    public String getName() {
        return "Now Playing";
    }


}
