package core.commands;

import dao.DaoImplementation;
import dao.entities.UrlCapsule;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.GuildMaker;
import core.parsers.NoOpParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class GuildTopCommand extends ConcurrentCommand {

	public GuildTopCommand(DaoImplementation dao) {
		super(dao);
		this.respondInPrivate = false;
		this.parser = new NoOpParser();

	}

	@Override
	public String getDescription() {
		return ("Chart 5x5 of guild most listened artist");
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("guild");
	}

	@Override
	public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		List<UrlCapsule> resultWrapper = getDao().getGuildTop(e.getGuild().getIdLong());
		BufferedImage image = GuildMaker.generateCollageThreaded(5, 5, new LinkedBlockingDeque<>(resultWrapper));
		sendImage(image, e);
	}

	@Override
	public String getName() {
		return "Guild Top Artists";
	}

}



