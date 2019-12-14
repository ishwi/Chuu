package core.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nonnull;

import core.Chuu;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.internal.JDAImpl;

public class CustomInterfacedEventManager implements IEventManager {

	private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
	private final Map<String, MyCommand> commandListeners = new HashMap<>();

	public CustomInterfacedEventManager() {

	}

	@Nonnull
	@Override
	public List<Object> getRegisteredListeners() {
		return Collections.unmodifiableList(new LinkedList<>(listeners));
	}

	/**
	 * @param event We are taking advantage from only using three types of events in
	 *              the whole bot , so we avoid less checking, for the momnent those
	 *              are MessageReceivedEvent, GuildMemberLeaveEvent and
	 *              MessageReactionAddEvent If you are using any other , pls modify
	 *              the code or use the default one
	 */
	@Override
	public void handle(@Nonnull GenericEvent event) {

		if (event instanceof MessageReceivedEvent) {
			MessageReceivedEvent mes = (MessageReceivedEvent) event;
			Character correspondingPrefix = Chuu.getCorrespondingPrefix(mes);
			String contentRaw = mes.getMessage().getContentRaw();
			if (contentRaw.length() <= 1 || contentRaw.charAt(0) != correspondingPrefix)
				return;
			String substring = contentRaw.substring(1).split("\\s+")[0];
			MyCommand myCommand = commandListeners.get(substring.toLowerCase());
			if (myCommand != null) {
				try {
					myCommand.onMessageReceived(mes);
				} catch (Throwable throwable) {
					JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", throwable);
				}
				return;
			}
			return;
		}
		if (event instanceof GuildMemberLeaveEvent || event instanceof MessageReactionAddEvent) {

			for (EventListener listener : listeners) {
				try {
					listener.onEvent(event);
				} catch (Throwable throwable) {
					JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", throwable);
				}
			}
		}
	}

	@Override
	public void register(@Nonnull Object listener) {
		if (!(listener instanceof EventListener))
			throw new IllegalArgumentException("Listener must implement EventListener");
		if ((listener instanceof MyCommand)) {
			MyCommand myCommand = (MyCommand) listener;
			List<String> aliases = myCommand.getAliases();
			for (String alias : aliases) {
				commandListeners.put(alias, myCommand);
			}

		}
		listeners.add(((EventListener) listener));
	}

	@Override
	public void unregister(@Nonnull Object listener) {
		if ((listener instanceof MyCommand)) {
			MyCommand myCommand = (MyCommand) listener;
			List<String> aliases = myCommand.getAliases();
			for (String alias : aliases) {
				commandListeners.remove(alias);
			}

		}
		listeners.remove(listener);
	}
}
