package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.UrlCapsule;
import main.ImageRenderer.GuildMaker;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class GuildTopCommand extends ConcurrentCommand {

	public GuildTopCommand(DaoImplementation dao) {
		super(dao);
		this.respondInPrivate = false;

	}

	@Override
	public void threadableCode(MessageReceivedEvent e) {
		List<UrlCapsule> resultWrapper = getDao().getGuildTop(e.getGuild().getIdLong());
		BufferedImage image = GuildMaker.generateCollageThreaded(5, 5, new LinkedBlockingDeque<>(resultWrapper));
		sendImage(image, e);
	}


	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!guild");
	}

	@Override
	public String getDescription() {
		return ("Chart 5x5 of guild most listened artist");
	}

	@Override
	public String getName() {
		return "Guild Top Artists";
	}

	@Override
	public List<String> getUsageInstructions() {
		return Collections.singletonList("!guild \n");
	}


}



