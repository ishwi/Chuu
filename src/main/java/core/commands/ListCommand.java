package core.commands;

import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public abstract class ListCommand<T, Y extends CommandParameters> extends ConcurrentCommand<Y> {


    public ListCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public abstract Parser<Y> initParser();

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        Y parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        List<T> list = getList(parse);
        printList(list, parse);
    }

    public abstract List<T> getList(Y params) throws LastFmException;

    public abstract void printList(List<T> list, Y params);
}
