package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.TimezoneParser;
import core.parsers.params.TimezoneParams;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class TimezoneCommand extends ConcurrentCommand<TimezoneParams> {

    TimezoneParser parser;

    public TimezoneCommand(ChuuService dao) {
        super(dao);
        this.parser = new TimezoneParser();
    }

    @Override
    public Parser<TimezoneParams> getParser() {
        return new TimezoneParser();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public List<String> getAliases() {
        return List.of("tz");
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        TimezoneParams parse = parser.parse(e);
        if (parse == null) return;


    }
}
