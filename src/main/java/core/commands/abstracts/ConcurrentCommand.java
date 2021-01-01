package core.commands.abstracts;

import core.apis.ExecutorsSingleton;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.ExecutorService;


public abstract class ConcurrentCommand<T extends CommandParameters> extends MyCommand<T> {
    public final ExecutorService executor = ExecutorsSingleton.getInstance();


    public ConcurrentCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    protected void measureTime(MessageReceivedEvent e) {
        executor.execute(() -> super.measureTime(e));
    }
}
