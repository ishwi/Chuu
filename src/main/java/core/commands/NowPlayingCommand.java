package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.parsers.params.NPMode;
import core.services.NPModeBuilder;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.NowPlayingArtist;
import dao.entities.ScrobbledArtist;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

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
    public void doSomethingWithArtist(NowPlayingArtist nowPlayingArtist, MessageReceivedEvent e, long discordId) {
        StringBuilder a = new StringBuilder();

        // Author fields cant have escaped markdown characters
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoNotStripped(e, discordId);

        String urlHolder = userInformation.getUrlImage();
        String userName = userInformation.getUsername();

        EnumSet<NPMode> npModes = getService().getNPModes(discordId);


        String title = String.format("%s's %s song:", userName, nowPlayingArtist.isNowPlaying() ? "current" : "last");
        String lastFMName = nowPlayingArtist.getUsername();


        a.append("**").append(CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.getArtistName()))
                .append("** | ").append(CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.getAlbumName())).append("\n");

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setAuthor(title, CommandUtil.getLastFmUser(lastFMName), urlHolder)
                .setThumbnail(CommandUtil.noImageUrl(nowPlayingArtist.getUrl()))
                .setTitle(CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.getSongName()), CommandUtil.getLastFMArtistTrack(nowPlayingArtist.getArtistName(), nowPlayingArtist.getSongName()))
                .setDescription(a);


        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(nowPlayingArtist.getArtistName(), 0, null);
        Long albumId = null;
        try {
            CommandUtil.validate(getService(), scrobbledArtist, lastFM, discogsApi, spotifyApi);
            if (nowPlayingArtist.getSongName() != null && !nowPlayingArtist.getSongName().isBlank())
                albumId = CommandUtil.albumvalidate(getService(), scrobbledArtist, lastFM, nowPlayingArtist.getAlbumName());
        } catch (LastFmException ignored) {

        }
        String serverName = e.isFromGuild() ? e.getGuild().getName() : null;
        String[] footerSpaces = new String[NPModeBuilder.getSize()];
        List<String> outputList = new ArrayList<>();
        Arrays.fill(footerSpaces, null);
        NPModeBuilder npModeBuilder = new NPModeBuilder(nowPlayingArtist, e, footerSpaces, discordId, userName, npModes, lastFMName, embedBuilder, scrobbledArtist, albumId, getService(), lastFM, serverName, mb, outputList);
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
        MessageBuilder messageBuilder = new MessageBuilder();
        embedBuilder.setFooter(footer, npModes.contains(NPMode.ARTIST_PIC) && !scrobbledArtist.getUrl().

                isBlank() ? scrobbledArtist.getUrl() : null);

        e.getChannel().

                sendMessage(messageBuilder.setEmbed(embedBuilder.build()).

                        build()).

                queue();

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
