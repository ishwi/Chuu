package core.commands;

import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.DiscordUserDisplay;
import dao.entities.UniqueWrapper;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class UniqueCommand extends ConcurrentCommand<ChuuDataParams> {
    public UniqueCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;

    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(getService());
    }

    @Override
    public String getDescription() {
        return ("Returns lists of all the unique artist you have scrobbled");
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("unique");
    }

    @Override
    public String getName() {
        return "Unique List Of Artists";
    }

    @Override
    public void onCommand(MessageReceivedEvent e, @NotNull ChuuDataParams params) throws LastFmException, InstanceNotFoundException {

        String lastFmName = params.getLastFMData().getName();

        UniqueWrapper<ArtistPlays> resultWrapper = getList(e.getGuild().getIdLong(), lastFmName);
        int rows = resultWrapper.getUniqueData().size();
        if (rows == 0) {
            sendMessageQueue(e, String.format("You have no %sunique artists :(", isGlobal() ? "global " : ""));
            return;
        }

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < rows; i++) {
            ArtistPlays g = resultWrapper.getUniqueData().get(i);
            a.append(i + 1).append(g.toString());
        }

        DiscordUserDisplay userInfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, resultWrapper.getDiscordId());


        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(e.getGuild().getIconUrl());
        embedBuilder.setDescription(a).setTitle(String.format("%s's Top 10%s unique artists", userInfo.getUsername(), isGlobal() ? " global" : ""), CommandUtil
                .getLastFmUser(lastFmName))
                .setThumbnail(userInfo.getUrlImage())
                .setFooter(String.format("%s has %d%s unique artists!%n", CommandUtil.markdownLessUserString(userInfo.getUsername(), resultWrapper.getDiscordId(), e), rows, isGlobal() ? " global" : ""), null);

        e.getChannel().sendMessage(embedBuilder.build()).queue(m ->
                new Reactionary<>(resultWrapper.getUniqueData(), m, embedBuilder));

    }

    public boolean isGlobal() {
        return false;
    }

    public UniqueWrapper<ArtistPlays> getList(long guildId, String lastFmName) {
        return getService().getUniqueArtist(guildId, lastFmName);
    }


}
