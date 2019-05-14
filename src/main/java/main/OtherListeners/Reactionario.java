package main.OtherListeners;

import main.Commands.CommandUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.List;

import static java.lang.Math.min;
import static java.lang.StrictMath.max;

public class Reactionario<T> extends ListenerAdapter {
	private final Message message;
	private final EmbedBuilder who;
	private final int pageSize;
	private List<T> list;
	private int counter = 0;

	public Reactionario(List<T> list, Message message, EmbedBuilder who) {
		this(list, message, who, 10);
	}

	public Reactionario(List<T> list, Message messageToReact, EmbedBuilder who, int pageSize) {
		this.list = list;
		this.message = messageToReact;
		this.who = who;
		this.pageSize = pageSize;
	}


	@Override
	public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {

		System.out.println("ASDHASIUDHAISUDHUIA");

		if (event.getUser().isBot() || event.getMessageIdLong() != message.getIdLong())
			return;
		int start;
		System.out.println(event.getReactionEmote().getEmoji());
		System.out.println(event.getReactionEmote().getAsCodepoints());


		switch (event.getReaction().getReactionEmote().getAsCodepoints()) {
			case "U+2b05":
				start = max(0, counter - pageSize);
				break;
			case "U+27a1":
				start = min(list.size() - (list.size() % pageSize), counter + pageSize);
				break;
			default:
				return;
		}
		System.out.println(start);
		System.out.println(counter);

		event.getReaction().removeReaction(event.getUser()).queue();


		StringBuilder a = new StringBuilder();
		for (int i = start; i < start + pageSize; i++) {
			a.append(i + 1).append(list.get(i).toString());
		}
		counter = start;
		System.out.println(counter);

		who.setDescription(a);
		who.setColor(CommandUtil.randomColor());

		message.editMessage(who.build()).queue();


	}
}
