package core.commands;

import dao.ChuuService;
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
        StringBuilder urlHolder = new StringBuilder();
        StringBuilder userNameHolder = new StringBuilder();

        CommandUtil.getUserInfoConsideringGuildOrNot(userNameHolder, urlHolder, e, discordId);

        userNameHolder.append(" 's ").append(nowPlayingArtist.isNowPlaying() ? "current" : "last").append(" song:");
        String username = nowPlayingArtist.getUsername();
        a.append("**").append(nowPlayingArtist.getArtistName())
                .append("** | ").append(nowPlayingArtist.getAlbumName());

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setAuthor(userNameHolder.toString(), CommandUtil.getLastFmUser(username), urlHolder.toString())
                .setThumbnail(CommandUtil.noImageUrl(nowPlayingArtist.getUrl()))
                .setTitle(nowPlayingArtist.getSongName(), CommandUtil.getUserArtistTrackUrl(nowPlayingArtist.getArtistName(), nowPlayingArtist.getSongName()))
                .setDescription(a);

        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
    }

    @Override
    public String getDescription() {
        return "Returns your current playing song";
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
