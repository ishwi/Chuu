package core.commands.whoknows;

import core.Chuu;
import core.commands.Context;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.imagerenderer.ChartQuality;
import core.imagerenderer.WhoKnowsMaker;
import core.parsers.OptionalEntity;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class GlobalBaseWhoKnowCommand<T extends CommandParameters> extends WhoKnowsBaseCommand<T> {
    public GlobalBaseWhoKnowCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = true;
        parser.addOptional(new OptionalEntity("nobotted", "discard users that have been manually flagged as potentially botted accounts"));
        parser.addOptional(new OptionalEntity("botted", "show botted accounts in case you have the config show-botted disabled"));
        parser.addOptional(new OptionalEntity("hideprivate", "only shows public users"));
        parser.addOptional(new OptionalEntity("hp", "use as a shorthand for hideprivate"));
    }


    @Override
    BufferedImage doImage(T ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        Context e = ap.getE();

        BufferedImage logo = null;
        String title = e.getJDA().getSelfUser().getName();
        if (e.isFromGuild()) {
            logo = CommandUtil.getLogo(db, e);
        }
        BufferedImage image = WhoKnowsMaker.generateWhoKnows(wrapperReturnNowPlaying, EnumSet.allOf(WKMode.class), title, logo, e.getAuthor().getIdLong());
        if (obtainPrivacyMode(ap) == PrivacyMode.NORMAL && CommandUtil.rand.nextFloat() >= 0.95f) {
            Character prefix = Chuu.getCorrespondingPrefix(e);
            DiscordUserDisplay uInfo = CommandUtil.getUserInfoNotStripped(e, e.getAuthor().getIdLong());
            EmbedBuilder embedBuilder = new ChuuEmbedBuilder()
                    .setTitle("Did you know?")
                    .setAuthor(uInfo.getUsername(), null, uInfo.getUrlImage())
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
                            PrivacyUtils.PrivateString privacy = PrivacyUtils.getPublicString(((GlobalReturnNowPlaying) x).getPrivacyMode(), x.getDiscordId(), x.getLastFMId(), atomicInteger, ap.getE(), showableUsers);
                            x.setDiscordName(privacy.discordName());
                            x.setLastFMId(privacy.lastfmName());
                            return ". " +
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

    abstract PrivacyMode obtainPrivacyMode(T params);

    boolean hidePrivate(T params) {
        return params.hasOptional("hp") || params.hasOptional("hideprivate");
    }


}
