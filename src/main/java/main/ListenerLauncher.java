package main;

import DAO.DaoImplementation;
import main.last.LastFMService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ListenerLauncher extends ListenerAdapter {
	private LastFMService lastAccess;
	private DaoImplementation impl;
	private  ExecutorService manager;
	public ListenerLauncher(LastFMService lastAccess, DaoImplementation impl) {
		this.lastAccess = lastAccess;
		this.impl = impl;
		this.manager = Executors.newCachedThreadPool();
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {




		if (event.getAuthor().isBot() || !event.getMessage().getContentRaw().startsWith("!")) {
			return;
		}
		manager.execute(new EventThreaded(event,impl,lastAccess));
	}


}
