package core.commands;

import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.ScrobbledArtist;
import dao.entities.StreakEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Credits: to lfmwhoknows bot owner for the idea
 */
public class StreakCommand extends ConcurrentCommand<ChuuDataParams> {
    public StreakCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> getParser() {
        return new OnlyUsernameParser(getService());
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
        ChuuDataParams params = parser.parse(e);
        if (params == null) {
            return;
        }
        String lastfmId = params.getLastFMData().getName();
        long discordID = params.getLastFMData().getDiscordId();


        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, discordID);
        String userName = userInformation.getUsername();
        String userUrl = userInformation.getUrlImage();
        StreakEntity combo = lastFM.getCombo(lastfmId);

        ScrobbledArtist artist = new ScrobbledArtist(combo.getCurrentArtist(), 0, "");
        CommandUtil.validate(getService(), artist, lastFM, DiscogsSingleton.getInstanceUsingDoubleLocking(), SpotifySingleton.getInstance());
        Long albumId = null;
        if (combo.getAlbCounter() > 1) {
            albumId = CommandUtil.albumvalidate(getService(), artist, lastFM, combo.getCurrentAlbum());
        }
        if (combo.getaCounter() >= 20) {
            if (combo.gettCounter() > 5050) {
                //Handle this case if it didnt existed
            }
            Long finalAlbumId = albumId;
            CompletableFuture.runAsync(() -> getService().insertCombo(combo, discordID, artist.getArtistId(), finalAlbumId));
        }


        int artistPlays = getService().getArtistPlays(artist.getArtistId(), lastfmId);
        String aString = CommandUtil.cleanMarkdownCharacter(artist.getArtist());
        StringBuilder description = new StringBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(String.format("%s 's current listening streak", CommandUtil.markdownLessUserString(userName, discordID, e)), CommandUtil.getLastFmUser(lastfmId), userUrl)
                .setThumbnail(CommandUtil.noImageUrl(artist.getUrl()))
                .setDescription("");

        if (combo.getaCounter() > 1) {
            description.append("**Artist**: ")
                    .append(combo.getaCounter()).append(combo.getaCounter() >= 5050 ? "+" : "").append(combo.getaCounter() != 1 ? " consecutive plays - " : " play - ")
                    .append("**[").append(aString).append("](").append(CommandUtil.getLastFmArtistUrl(combo.getCurrentArtist())).append(")**").append("\n");
        }
        if (combo.getAlbCounter() > 1) {
            description.append("**Album**: ")
                    .append(combo.getAlbCounter())
                    .append(combo.getAlbCounter() >= 5050 ? "+" : "")
                    .append(combo.getAlbCounter() != 1 ? " consecutive plays - " : " play - ")
                    .append("**[").append(CommandUtil.cleanMarkdownCharacter(combo.getCurrentAlbum())).append("](")
                    .append(CommandUtil.getLastFmArtistAlbumUrl(combo.getCurrentArtist(), combo.getCurrentAlbum())).append(")**")
                    .append("\n");
        }
        if (combo.gettCounter() > 1) {
            description.append("**Song**: ").append(combo.gettCounter()).append(combo.gettCounter() >= 5050 ? "+" : "")
                    .append(combo.gettCounter() != 1 ? " consecutive plays - " : " play - ").append("**[")
                    .append(CommandUtil.cleanMarkdownCharacter(combo.getCurrentSong())).append("](").append(CommandUtil.getLastFMArtistTrack(combo.getCurrentArtist(), combo.getCurrentSong())).append(")**").append("\n");
        }

        MessageEmbed build = embedBuilder.setDescription(description)
                .setColor(CommandUtil.randomColor())
                .setFooter(String.format("%s has played %s %d %s!", CommandUtil.markdownLessUserString(userName, discordID, e), artist.getArtist(), artistPlays, CommandUtil.singlePlural(artistPlays, "time", "times")))
                .build();
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setEmbed(build).

                sendTo(e.getChannel()).

                queue();

    }
}
