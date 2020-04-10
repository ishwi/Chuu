package core.commands;

import core.apis.ExecutorsSingleton;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.ExecutorService;


abstract class ConcurrentCommand extends MyCommand {
    final ExecutorService executor = ExecutorsSingleton.getInstance();


    public ConcurrentCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    protected void measureTime(MessageReceivedEvent e) {
        executor.execute(() -> {
                    long startTime = System.currentTimeMillis();
                    handleCommand(e);
                    long endTime = System.currentTimeMillis();
                    long timeElapsed = endTime - startTime;
                    System.out.println("Execution time in milliseconds " + getName() + " : " + timeElapsed);
                    System.out.println();
                }
        );
    }
}
