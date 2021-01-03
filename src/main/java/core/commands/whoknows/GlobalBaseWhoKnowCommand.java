package core.commands.whoknows;

import core.Chuu;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.imagerenderer.ChartQuality;
import core.imagerenderer.WhoKnowsMaker;
import core.parsers.OptionalEntity;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class GlobalBaseWhoKnowCommand<T extends CommandParameters> extends WhoKnowsBaseCommand<T> {
    public GlobalBaseWhoKnowCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = true;
        parser.addOptional(new OptionalEntity("nobotted", "discard users that have been manually flagged as potentially botted accounts"));
        parser.addOptional(new OptionalEntity("botted", "show botted accounts in case you have the config show-botted disabled"));

    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_STATS;
    }

    @Override
    BufferedImage doImage(T ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        MessageReceivedEvent e = ap.getE();

        BufferedImage logo = null;
        String title = e.getJDA().getSelfUser().getName();
        if (e.isFromGuild()) {
            logo = CommandUtil.getLogo(getService(), e);
        }
        BufferedImage image = WhoKnowsMaker.generateWhoKnows(wrapperReturnNowPlaying, title, logo);
        if (obtainPrivacyMode(ap) == PrivacyMode.NORMAL && CommandUtil.rand.nextFloat() >= 0.95f) {
            Character prefix = Chuu.getCorrespondingPrefix(e);
            DiscordUserDisplay uInfo = CommandUtil.getUserInfoNotStripped(e, e.getAuthor().getIdLong());
            EmbedBuilder embedBuilder = new EmbedBuilder()
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
            showableUsers = getService().getAll(ap.getE().getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toSet());
            showableUsers.add(author);
        } else {
            showableUsers = Set.of(author);
        }
        AtomicInteger atomicInteger = new AtomicInteger(1);
        Consumer<GlobalReturnNowPlaying> a = (x) -> {
            PrivacyMode privacyMode = x.getPrivacyMode();
            if (showableUsers.contains(x.getDiscordId())) {
                privacyMode = PrivacyMode.DISCORD_NAME;
            }
            switch (privacyMode) {
                case STRICT, NORMAL -> {
                    x.setDiscordName("Private User #" + atomicInteger.getAndIncrement());
                    x.setLastFMId(Chuu.DEFAULT_LASTFM_ID);
                }
                case DISCORD_NAME -> {
                    x.setDiscordName(CommandUtil.getUserInfoNotStripped(ap.getE(), x.getDiscordId()).getUsername());
                    x.setLastFMId(Chuu.getLastFmId(x.getLastFMId()));
                }
                case TAG -> {
                    x.setDiscordName(ap.getE().getJDA().retrieveUserById(x.getDiscordId()).complete().getAsTag());
                    x.setLastFMId(Chuu.getLastFmId(x.getLastFMId()));
                }
                case LAST_NAME -> {
                    x.setDiscordName(x.getLastFMId() + " (last.fm)");
                    x.setLastFMId(Chuu.getLastFmId(x.getLastFMId()));
                }
            }
            String itemUrl = PrivacyUtils.getUrlTitle(x);
            x.setItemUrl(itemUrl);
        };
        wrapperReturnNowPlaying.getReturnNowPlayings()
                .forEach(x ->
                        {
                            GlobalReturnNowPlaying x1 = (GlobalReturnNowPlaying) x;
                            x1.setGlobalDisplayer(a);
                        }
                );
        switch (effectiveMode) {
            case IMAGE -> doImage(ap, wrapperReturnNowPlaying);
            case LIST -> doList(ap, wrapperReturnNowPlaying);
            case PIE -> doPie(ap, wrapperReturnNowPlaying);
        }
    }

    abstract PrivacyMode obtainPrivacyMode(T params);


}
