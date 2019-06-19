package main.Commands;

import DAO.DaoImplementation;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public abstract class ConcurrentCommand extends MyCommandDbAccess {

	public ConcurrentCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public void onCommand(MessageReceivedEvent e, String[] args) {
		final ExecutorService executor = Executors.newCachedThreadPool();

		executor.submit(() -> run(e));
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		Message message = e.getMessage();
		if (!message.getContentDisplay().startsWith(PREFIX) || (e.getAuthor().isBot() && !respondToBots()))
			return;
		if (containsCommand(e.getMessage())) {
			e.getChannel().sendTyping().queue();
			System.out.println("We received a message from " +
					e.getAuthor().getName() + "; " + e.getMessage().getContentDisplay());
			if (!e.getChannelType().isGuild() && !respondInPrivate) {
				sendMessage(e, "This commmand only works in a server");
				return;
			}
			onCommand(e, commandArgs(e.getMessage()));

		}
	}

	public abstract void threadablecode(MessageReceivedEvent e);

	public void run(MessageReceivedEvent e) {

		long startTime = System.currentTimeMillis();
		threadablecode(e);
		long endTime = System.currentTimeMillis();
		long timeElapsed = endTime - startTime;
		System.out.println("Execution time in milliseconds " + getName() + " : " + timeElapsed);
		System.out.println();

	}
}
