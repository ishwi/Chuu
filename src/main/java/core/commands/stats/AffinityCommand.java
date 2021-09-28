package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.imagerenderer.LoveMaker;
import core.otherlisteners.Reactionary;
import core.parsers.AffinityParser;
import core.parsers.Parser;
import core.parsers.params.AffinityParameters;
import core.services.UserInfoService;
import dao.ServiceView;
import dao.entities.Affinity;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;

/**
 * Credits: to lfmwhoknows bot owner for the idea
 */
public class AffinityCommand extends ConcurrentCommand<AffinityParameters> {
    public static final int DEFAULT_THRESHOLD = 30;

    public AffinityCommand(ServiceView dao) {
        super(dao, true);
        this.respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<AffinityParameters> initParser() {
        return new AffinityParser(db);
    }

    @Override
    public String getDescription() {
        return "Gets your affinity with a user or with the rest of the server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("affinity", "aff", "soulmate");
    }

    @Override
    public String getName() {
        return "Affinity";
    }

    @Override
    protected void onCommand(Context e, @Nonnull AffinityParameters params) throws LastFmException, InstanceNotFoundException {
        if (params.isDoServer()) {
            doGuild(params);
        } else {
            doIndividual(params);
        }
    }

    void doIndividual(AffinityParameters ap) throws LastFmException {
        Context e = ap.getE();

        Affinity affinity = db.getAffinity(ap.getFirstUser().getName(), ap.getSecondUser().getName(), ap.getThreshold());
        DiscordUserDisplay first = CommandUtil.getUserInfoUnescaped(e, ap.getFirstDiscordID());
        DiscordUserDisplay second = CommandUtil.getUserInfoUnescaped(e, ap.getSecondDiscordID());
        UserInfoService info = new UserInfoService(db);
        BufferedImage bufferedImage = LoveMaker.calculateLove(affinity, first, info.maybeRefresh(ap.getFirstUser()).getImage(), info.maybeRefresh(ap.getSecondUser()).getImage(), second);
        sendImage(bufferedImage, e);
    }

    void doGuild(AffinityParameters ap) throws InstanceNotFoundException {
        Context e = ap.getE();
        LastFMData ogData = db.findLastFMData(e.getAuthor().getIdLong());
        List<Affinity> serverAff = db.getServerAffinity(ogData.getName(), e.getGuild().getIdLong(), ap.getThreshold()).stream()
                .sorted(Comparator.comparing(Affinity::getAffinity).reversed()).toList();
        if (serverAff.isEmpty()) {
            if (ap.getThreshold() != DEFAULT_THRESHOLD) {
                sendMessageQueue(e, "You don't have any matching artist with more than %d %s with anyone in this server.".formatted(ap.getThreshold(), CommandUtil.singlePlural(ap.getThreshold(), "play", "plays")));
            } else {
                sendMessageQueue(e, "You don't have any matching artist (using the default threshold of %d %s) with anyone in this server. You can try specifying a lower value!"
                        .formatted(ap.getThreshold(), CommandUtil.singlePlural(ap.getThreshold(), "play", "plays")));
            }
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        List<String> lines = serverAff.stream().map(x -> String.format(". [%s](%s) - %.2f%%%s matching%n", getUserString(e, x.getDiscordId()),
                CommandUtil.getLastFmUser(x.getReceivingLastFmId()),
                (x.getAffinity() > 1 ? 1 : x.getAffinity()) * 100, x.getAffinity() > 1 ? "+" : "")).toList();
        for (int i = 0, size = lines.size(); i < 10 && i < size; i++) {
            String text = lines.get(i);
            stringBuilder.append(i + 1).append(text);
        }
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, e.getAuthor().getIdLong());
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setDescription(stringBuilder)
                .setAuthor(uInfo.username() + "'s soulmates in " + e.getGuild().getName(), PrivacyUtils.getLastFmUser(ogData.getName()), uInfo.urlImage())
                .setFooter(String.format("%s's affinity using a threshold of %d plays!%n", uInfo.username(), ap.getThreshold()), null)
                .setThumbnail(e.getGuild().getIconUrl());
        e.sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(lines, message1, embedBuilder));
    }

}
