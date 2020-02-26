package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.GuildMaker;
import core.parsers.OnlyChartSizeParser;
import core.parsers.OptionalEntity;
import dao.ChuuService;
import dao.entities.UrlCapsule;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class GuildTopCommand extends ConcurrentCommand {

    public GuildTopCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
        this.parser = new OnlyChartSizeParser(new OptionalEntity("--global", " from all bot users instead of only from this server"));

    }

    @Override
    public String getDescription() {
        return ("Chart 5x5 of guild most listened artist");
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("guild", "server", "general");
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        int x = Integer.parseInt(parse[0]);
        int y = Integer.parseInt(parse[1]);
        boolean global = Boolean.parseBoolean(parse[2]);

        List<UrlCapsule> resultWrapper = getService().getGuildTop(global ? null : e.getGuild().getIdLong(), x * y);
        BufferedImage image = GuildMaker.generateCollageThreaded(x, y, new LinkedBlockingDeque<>(resultWrapper));
        sendImage(image, e);
    }

    @Override
    public String getName() {
        return "Guild Top Artists";
    }

}



