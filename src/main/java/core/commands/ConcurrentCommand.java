package core.commands;

import dao.DaoImplementation;
import core.apis.ExecutorsSingleton;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.ExecutorService;


abstract class ConcurrentCommand extends MyCommand {
	final ExecutorService executor = ExecutorsSingleton.getInstanceUsingDoubleLocking();


	ConcurrentCommand(DaoImplementation dao) {
		super(dao);
	}


	@Override
	void measureTime(MessageReceivedEvent e) {
		executor.submit(() -> {
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