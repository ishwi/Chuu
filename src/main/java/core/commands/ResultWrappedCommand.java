package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.ResultWrapper;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class ResultWrappedCommand<T> extends ConcurrentCommand {

    ResultWrappedCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        printList(getList(e), e);
    }

    public abstract ResultWrapper<T> getList(MessageReceivedEvent e);

    public abstract void printList(ResultWrapper<T> list, MessageReceivedEvent e);

}
