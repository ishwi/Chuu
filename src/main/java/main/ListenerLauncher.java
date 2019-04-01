package main;

import DAO.DaoImplementation;
import main.last.LastFMService;
import main.last.UpdaterThread;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


class ListenerLauncher extends ListenerAdapter {
	private final LastFMService lastAccess;
	private final DaoImplementation impl;
	private final ExecutorService manager;
	private final Spotify spotify;
	private final ScheduledExecutorService scheduledManager;


	public ListenerLauncher(LastFMService lastAccess, DaoImplementation impl, Spotify spotify) {
		this.lastAccess = lastAccess;
		this.impl = impl;
		this.spotify = spotify;
		this.manager = Executors.newCachedThreadPool();
		this.scheduledManager = Executors.newScheduledThreadPool(1);
		scheduledManager.scheduleAtFixedRate(new UpdaterThread(this.impl, this.lastAccess), 0, 15, TimeUnit.MINUTES);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {


		if (event.getAuthor().isBot() || !event.getMessage().getContentRaw().startsWith("!")) {
			return;
		}
		manager.execute(new EventThreaded(event, impl, lastAccess, this.spotify));
	}


}
