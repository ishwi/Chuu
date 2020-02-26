package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public abstract class ListCommand<T> extends ConcurrentCommand {


    public ListCommand(ChuuService dao) {
        super(dao);
        this.parser = new NoOpParser();
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        List<T> list = getList(parse, e);
        printList(list, e);
    }

    public abstract List<T> getList(String[] message, MessageReceivedEvent e) throws LastFmException;

    public abstract void printList(List<T> list, MessageReceivedEvent e);
}