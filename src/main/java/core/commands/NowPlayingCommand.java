package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import core.services.NPModeBuilder;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.NPMode;
import dao.entities.NowPlayingArtist;
import dao.entities.ScrobbledArtist;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
    public void doSomethingWithArtist(NowPlayingArtist nowPlayingArtist, MessageReceivedEvent e, long discordId) {
        StringBuilder a = new StringBuilder();

        // Author fields cant have escaped markdown characters
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoNotStripped(e, discordId);

        String urlHolder = userInformation.getUrlImage();
        String userName = userInformation.getUsername();

        EnumSet<NPMode> npModes = EnumSet.noneOf(NPMode.class);
        if (e.isFromGuild()) {
            npModes = getService().getServerNPModes(e.getGuild().getIdLong());
        }

        if (npModes.isEmpty() || npModes.size() == 1 && npModes.contains(NPMode.UNKNOWN)) {
            npModes = getService().getNPModes(discordId);
        }

        String title = String.format("%s's %s song:", userName, nowPlayingArtist.isNowPlaying() ? "current" : "last");
        String lastFMName = nowPlayingArtist.getUsername();


        a.append("**").append(CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.getArtistName()))
                .append("** | ").append(CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.getAlbumName())).append("\n");

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setAuthor(title, CommandUtil.getLastFmUser(lastFMName), urlHolder)
                .setThumbnail(CommandUtil.noImageUrl(nowPlayingArtist.getUrl()))
                .setTitle(CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.getSongName()), LinkUtils.getLastFMArtistTrack(nowPlayingArtist.getArtistName(), nowPlayingArtist.getSongName()))
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
        if (npModes.contains(NPMode.RANDOM)) {
            List<NPMode> allModes = EnumSet.allOf(NPMode.class).stream().filter(x -> !x.equals(NPMode.UNKNOWN)).filter(x -> !x.equals(NPMode.RANDOM)).collect(Collectors.toList());

            npModes = EnumSet.copyOf(IntStream.range(0, CommandUtil.rand.nextInt(4) + 1).mapToObj(x -> allModes.get(CommandUtil.rand.nextInt(allModes.size()))).collect(Collectors.toSet()));
        }
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
        e.getChannel().sendMessage(embedBuilder.build()).queue();

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
