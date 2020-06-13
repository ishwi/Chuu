package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.WhoKnowsMaker;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.UsersWrapper;
import dao.entities.WhoKnowsMode;
import dao.entities.WrapperReturnNowPlaying;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GlobalWhoKnowCommand extends WhoKnowsCommand {
    public GlobalWhoKnowCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public Parser<ArtistParameters> getParser() {
        Parser<ArtistParameters> parser = super.getParser();
        parser.addOptional(new OptionalEntity("--nobotted", "discard users that have been manually flagged as potentially botted accounts"));
        return parser;
    }

    @Override
    public String getName() {
        return "Global Who Knows";
    }

    @Override
    public String getDescription() {
        return "Like who knows but for all bot users and keeping some privacy :flushed:";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globalwhoknows", "gk", "gwk");
    }

    @Override
    void doImage(ArtistParameters ap, WrapperReturnNowPlaying wrapperReturnNowPlaying) {
        MessageReceivedEvent e = ap.getE();
        BufferedImage image = WhoKnowsMaker.generateWhoKnows(wrapperReturnNowPlaying, e.getJDA().getSelfUser().getName(), null);
        sendImage(image, e);
    }

    @Override
    void whoKnowsLogic(ArtistParameters ap) {
        ScrobbledArtist who = ap.getScrobbledArtist();
        long artistId = who.getArtistId();
        WhoKnowsMode effectiveMode = getEffectiveMode(ap.getLastFMData().getWhoKnowsMode(), ap);

        boolean b = ap.hasOptional("--nobotted");
        long author = ap.getE().getAuthor().getIdLong();
        WrapperReturnNowPlaying wrapperReturnNowPlaying =
                effectiveMode.equals(WhoKnowsMode.IMAGE) ? this.getService().globalWhoKnows(artistId, !b, author) : this.getService().globalWhoKnows(artistId, Integer.MAX_VALUE, !b, author);
        if (wrapperReturnNowPlaying.getRows() == 0) {
            sendMessageQueue(ap.getE(), "No one knows " + CommandUtil.cleanMarkdownCharacter(who.getArtist()));
            return;
        }
        Set<Long> showableUsers;
        if (ap.getE().isFromGuild()) {
            showableUsers = getService().getAll(ap.getE().getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toSet());
            showableUsers.add(author);
        } else {
            showableUsers = Set.of(author);
        }
        AtomicInteger atomicInteger = new AtomicInteger(1);
        wrapperReturnNowPlaying.getReturnNowPlayings()
                .forEach(x -> {
                            if (showableUsers.contains(x.getDiscordId())) {
                                x.setDiscordName(CommandUtil.getUserInfoNotStripped(ap.getE(), x.getDiscordId()).getUsername());
                            } else {
                                x.setDiscordName("Private User #" + atomicInteger.getAndIncrement());
                            }
                        }
                );
        wrapperReturnNowPlaying.setUrl(who.getUrl());
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
