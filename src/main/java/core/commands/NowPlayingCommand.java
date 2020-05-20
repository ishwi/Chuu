package core.commands;

import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import dao.entities.NowPlayingArtist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class NowPlayingCommand extends NpCommand {
    public NowPlayingCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    public void doSomethingWithArtist(NowPlayingArtist nowPlayingArtist, MessageReceivedEvent e, long discordId) {
        StringBuilder a = new StringBuilder();

        // Author fields cant have escaped markdown characters
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoNotStripped(e, discordId);

        String urlHolder = userInformation.getUrlImage();
        String userName = userInformation.getUsername();

        String title = String.format("%s's %s song:", userName, nowPlayingArtist.isNowPlaying() ? "current" : "last");
        String lastFMName = nowPlayingArtist.getUsername();

        a.append("**").append(CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.getArtistName()))
                .append("** | ").append(CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.getAlbumName())).append("\n");

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setAuthor(title, CommandUtil.getLastFmUser(lastFMName), urlHolder)
                .setThumbnail(CommandUtil.noImageUrl(nowPlayingArtist.getUrl()))
                .setTitle(CommandUtil.cleanMarkdownCharacter(nowPlayingArtist.getSongName()), CommandUtil.getLastFMArtistTrack(nowPlayingArtist.getArtistName(), nowPlayingArtist.getSongName()))
                .setDescription(a);

        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
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
