package core.commands;

import core.imagerenderer.WhoKnowsMaker;
import core.parsers.OptionalEntity;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class GlobalBaseWhoKnowCommand<T extends CommandParameters> extends WhoKnowsBaseCommand<T> {
    public GlobalBaseWhoKnowCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = true;
        parser.addOptional(new OptionalEntity("nobotted", "discard users that have been manually flagged as potentially botted accounts"));
    }


    @Override
    protected CommandCategory getCategory() {
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
        sendImage(image, e);

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
                case STRICT:
                case NORMAL:
                    x.setDiscordName("Private User #" + atomicInteger.getAndIncrement());
                    break;
                case DISCORD_NAME:
                    x.setDiscordName(CommandUtil.getUserInfoNotStripped(ap.getE(), x.getDiscordId()).getUsername());
                    break;
                case TAG:
                    x.setDiscordName(ap.getE().getJDA().retrieveUserById(x.getDiscordId()).complete().getAsTag());
                    break;
                case LAST_NAME:
                    x.setDiscordName(x.getLastFMId() + " (last.fm)");
                    break;
            }

        };
        wrapperReturnNowPlaying.getReturnNowPlayings()
                .forEach(x ->
                        {
                            GlobalReturnNowPlaying x1 = (GlobalReturnNowPlaying) x;
                            x1.setDisplayer(a);
                        }
                );
        switch (effectiveMode) {
            case IMAGE:
                doImage(ap, wrapperReturnNowPlaying);
                break;
            case LIST:
                doList(ap, wrapperReturnNowPlaying);
                break;
            case PIE:
                doPie(ap, wrapperReturnNowPlaying);
                break;
        }
    }


}
