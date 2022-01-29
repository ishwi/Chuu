package core.commands.whoknows;

import core.Chuu;
import core.commands.Context;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.imagerenderer.ChartQuality;
import core.imagerenderer.WhoKnowsMaker;
import core.parsers.params.CommandParameters;
import core.parsers.utils.OptionalEntity;
import core.parsers.utils.Optionals;
import dao.ServiceView;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class GlobalBaseWhoKnowCommand<T extends CommandParameters> extends WhoKnowsBaseCommand<T> {
    public GlobalBaseWhoKnowCommand(ServiceView dao) {
        super(dao);
        this.respondInPrivate = true;
        parser.addOptional(Optionals.NOBOTTED.opt);
        parser.addOptional(Optionals.BOTTED.opt);
        parser.addOptional(new OptionalEntity("hideprivate", "only shows public users", "hp"));
    }


    @Override
    BufferedImage doImage(T ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        Context e = ap.getE();

        BufferedImage logo = null;
        String title = e.getJDA().getSelfUser().getName();
        if (e.isFromGuild()) {
            logo = CommandUtil.getLogo(db, e);
        }
        handleWkMode(ap, wrapperReturnNowPlaying);
        BufferedImage image = WhoKnowsMaker.generateWhoKnows(wrapperReturnNowPlaying, EnumSet.allOf(WKMode.class), title, logo, e.getAuthor().getIdLong());
        if (obtainLastFmData(ap).getPrivacyMode() == PrivacyMode.NORMAL && CommandUtil.rand.nextFloat() >= 0.95f) {
            char prefix = e.getPrefix();
            DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, e.getAuthor().getIdLong());
            EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                    .setTitle("Did you know?")
                    .setAuthor(uInfo.username(), null, uInfo.urlImage())
                    .setDescription(MessageFormat.format("Your privacy setting is set to **normal**. This means people outside this server cannot see you on the global leaderboards. To show your name do {0}privacy and change your setting to **Tag**, **Last-Name** or **Discord-Name**. Do {1}help privacy for extra info.", prefix, prefix));
            sendImage(image, e, ChartQuality.PNG_BIG, embedBuilder);
        } else {
            sendImage(image, e);
        }
        return logo;
    }

    @Override
    public void generateWhoKnows(WrapperReturnNowPlaying wrapperReturnNowPlaying, T ap, long author, WhoKnowsMode effectiveMode) {
        Set<Long> showableUsers;
        if (ap.getE().isFromGuild()) {
            showableUsers = db.getAll(ap.getE().getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toSet());
            showableUsers.add(author);
        } else {
            showableUsers = Set.of(author);
        }
        AtomicInteger atomicInteger = new AtomicInteger(1);
        wrapperReturnNowPlaying.getReturnNowPlayings()
                .forEach(x ->
                        x.setGenerateString(() -> {
                            PrivacyMode privacyMode = ((GlobalReturnNowPlaying) x).getPrivacyMode();
                            PrivacyUtils.PrivateString privacy = PrivacyUtils.getPublicString(privacyMode, x.getDiscordId(), x.getLastFMId(), atomicInteger, ap.getE(), showableUsers);
                            privacy = mapPrivacy(x, privacy, ap, privacyMode, showableUsers);
                            x.setDiscordName(privacy.discordName());
                            x.setLastFMId(privacy.lastfmName());
                            return x.getIndex() + 1 + ". " +
                                    "**[" + privacy.discordName() + "](" +
                                    PrivacyUtils.getUrlTitle(x) +
                                    ")** - " +
                                    x.getPlayNumber() + " plays\n";
                        })
                );
        switch (effectiveMode) {
            case IMAGE -> doImage(ap, wrapperReturnNowPlaying);
            case LIST -> doList(ap, wrapperReturnNowPlaying);
            case PIE -> doPie(ap, wrapperReturnNowPlaying);
        }
    }

    public PrivacyUtils.PrivateString mapPrivacy(ReturnNowPlaying x, PrivacyUtils.PrivateString privateString, T ap, PrivacyMode privacyMode, Set<Long> ids) {
        LastFMData lastFMData = obtainLastFmData(ap);
        if (lastFMData.getRole() == Role.ADMIN && (privacyMode == PrivacyMode.NORMAL || privacyMode == PrivacyMode.STRICT) &&
                (!ap.getE().isFromGuild() || ap.getE().getChannel().getIdLong() == Chuu.channel2Id)) {
            return new PrivacyUtils.PrivateString("Private: " + x.getLastFMId(), x.getLastFMId());
        }
        return privateString;
    }

    boolean hidePrivate(T params) {
        return params.hasOptional("hideprivate");
    }


    abstract LastFMData obtainLastFmData(T params);
}
