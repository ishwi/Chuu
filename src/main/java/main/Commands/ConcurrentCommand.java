package main.Commands;

import DAO.DaoImplementation;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


public abstract class ConcurrentCommand extends MyCommandDbAccess implements Runnable {
	public MessageReceivedEvent e;

	public ConcurrentCommand(DaoImplementation dao) {
		super(dao);
	}



	@Override
	public void onCommand(MessageReceivedEvent e, String[] args) {
		this.e = e;
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot() && !respondToBots())
			return;
		if (containsCommand(e.getMessage())) {
			System.out.println("We received a message from " +
					e.getAuthor().getName() + "; " + e.getMessage().getContentDisplay());

			onCommand(e, commandArgs(e.getMessage()));

		}
	}

	public abstract void threadableCode();

	public void run() {
		MessageReceivedEvent e = this.e;
		long startTime = System.currentTimeMillis();
		threadableCode();
		long endTime = System.currentTimeMillis();
		long timeElapsed = endTime - startTime;
		System.out.println("Execution time in milliseconds " + getName() + " : " + timeElapsed);
		System.out.println();

	}
}
