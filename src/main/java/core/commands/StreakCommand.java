package core.commands;

import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.StreakEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * Credits: to lfmwhoknows bot owner for the idea
 */
public class StreakCommand extends ConcurrentCommand {
    public StreakCommand(ChuuService dao) {
        super(dao);
        this.parser = new OnlyUsernameParser(dao);
    }

    @Override
    public String getDescription() {
        return "Last playing streak";
    }

    @Override
    public List<String> getAliases() {
        return List.of("streak", "combo");
    }

    @Override
    public String getName() {
        return "Streak";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        String lastfmId = parse[0];
        long discordID = Long.parseLong(parse[1]);
        StringBuilder userName = new StringBuilder();
        StringBuilder userUrl = new StringBuilder();

        CommandUtil.getUserInfoConsideringGuildOrNot(userName, userUrl, e, discordID);
        StreakEntity combo = lastFM.getCombo(lastfmId);
        ScrobbledArtist artist = new ScrobbledArtist(combo.getCurrentArtist(), 0, "");
        CommandUtil.validate(getService(), artist, lastFM, DiscogsSingleton.getInstanceUsingDoubleLocking(), SpotifySingleton.getInstanceUsingDoubleLocking());
        int artistPlays = getService().getArtistPlays(artist.getArtistId(), lastfmId);
        StringBuilder description = new StringBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(userName.toString() + " 's current listening streak", CommandUtil.getLastFmUser(lastfmId), userUrl.toString())
                .setThumbnail(CommandUtil.noImageUrl(artist.getUrl()))
                .setDescription("");
        description.append("**Artist**: ").append(combo.getaCounter()).append(combo.getaCounter() >= 1000 ? "+" : "").append(combo.getaCounter() != 1 ? " consecutive plays - " : " play - ").append("**[")
                .append(artist.getArtist()).append("](").append(CommandUtil.getLastFmArtistUrl(combo.getCurrentArtist())).append(")**").append("\n");

        if (combo.getAlbCounter() > 0) {
            description.append("**Album**: ").append(combo.getAlbCounter()).append(combo.getAlbCounter() >= 1000 ? "+" : "").append(combo.getAlbCounter() != 1 ? " consecutive plays - " : " play - ").append("**[")
                    .append(combo.getCurrentAlbum()).append("](").append(CommandUtil.getLastFmArtistAlbumUrl(combo.getCurrentArtist(), combo.getCurrentAlbum())).append(")**").append("\n");
        }
        if (combo.gettCounter() > 0) {
            description.append("**Song**: ").append(combo.gettCounter()).append(combo.gettCounter() >= 1000 ? "+" : "").append(combo.gettCounter() != 1 ? " consecutive plays - " : " play - ").append("**[")
                    .append(combo.getCurrentSong()).append("](").append(CommandUtil.getLastFMArtistTrack(combo.getCurrentArtist(), combo.getCurrentSong())).append(")**").append("\n");
        }
        MessageEmbed build = embedBuilder.setDescription(description)
                .setColor(CommandUtil.randomColor())
                .setFooter(userName.toString() + " has played " + artist.getArtist() + " " + artistPlays + " times!")
                .build();
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setEmbed(build).sendTo(e.getChannel()).queue();

    }
}
