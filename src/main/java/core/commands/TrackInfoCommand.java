package core.commands;

import core.apis.last.TrackExtended;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.ArtistSongParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.stream.Collectors;

public class TrackInfoCommand extends AlbumPlaysCommand {
    public TrackInfoCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.INFO;
    }

    @Override
    public Parser<ArtistAlbumParameters> getParser() {
        return new ArtistSongParser(getService(), lastFM);
    }

    @Override
    public String getDescription() {
        return "Information about a track";
    }

    @Override
    public List<String> getAliases() {
        return List.of("trackinfo", "ti");
    }

    @Override
    public String getName() {
        return "Track Info";
    }

    @Override
    void doSomethingWithAlbumArtist(ScrobbledArtist artist, String song, MessageReceivedEvent e, long who) throws InstanceNotFoundException, LastFmException {
        LastFMData lastFMData = getService().findLastFMData(who);
        TrackExtended trackInfo = this.lastFM.getTrackInfoExtended(lastFMData.getName(), artist.getArtist(), song);


        String username = getUserString(e, who, lastFMData.getName());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        String tagsField = trackInfo.getTags().isEmpty()
                ? ""
                : trackInfo.getTags().stream()
                .map(tag -> String.format("[%s](%s)", CommandUtil.cleanMarkdownCharacter(tag), CommandUtil.getLastFmTagUrl(tag)))
                .collect(Collectors.joining(" - "));

        MessageBuilder messageBuilder = new MessageBuilder();
        embedBuilder.setTitle(trackInfo.getName(), CommandUtil.getLastFMArtistTrack(trackInfo.getArtist(), trackInfo.getName()))
                .addField("Artist:", String.format("[%s](%s)", CommandUtil.cleanMarkdownCharacter(trackInfo.getArtist()),
                        CommandUtil.getLastFmArtistUrl(trackInfo.getArtist())), false);
        if (trackInfo.getAlbumName() != null) {
            embedBuilder.
                    addField("Album:",
                            String.format("[%s](%s)", CommandUtil.cleanMarkdownCharacter(trackInfo.getAlbumName()), CommandUtil.getLastFmArtistAlbumUrl(trackInfo.getArtist(), trackInfo.getAlbumName())),
                            false);
        }
        embedBuilder
                .addField(username + "'s plays:", String.valueOf(trackInfo.getPlays()), true)
                .addField("Loved?", trackInfo.isLoved() ? ":heart:" : ":black_heart: ", true)

                .addField("Listeners:", String.valueOf(trackInfo.getListeners()), true)
                .addField("Scrobbles:", String.valueOf(trackInfo.getTotalPlayCount()), true)
                .addField("Tags:", tagsField, false)

                .addField("Duration:",
                        (String.format("%02d:%02d minutes", trackInfo.getDuration() / 60, trackInfo.getDuration() % 60))
                        , true);

        embedBuilder.setImage(trackInfo.getImageUrl() == null || trackInfo.getImageUrl().isBlank() ? null : trackInfo.getImageUrl())
                .setColor(CommandUtil.randomColor())
                .setThumbnail(artist.getUrl());
        messageBuilder.setEmbed(embedBuilder.build()).
                sendTo(e.getChannel()).
                queue();

    }
}
