package core.commands;

import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public abstract class ListCommand<T> extends ConcurrentCommand {


    ListCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public void onCommand(MessageReceivedEvent e) {
        List<T> list = getList(e);
        printList(list, e);
    }

    public abstract List<T> getList(MessageReceivedEvent e);

    public abstract void printList(List<T> list, MessageReceivedEvent e);
}