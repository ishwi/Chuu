package main.commands;

import dao.DaoImplementation;
import main.exceptions.InstanceNotFoundException;
import main.exceptions.LastFmException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class AlbumsOfTheDecadeCommand extends MusicBrainzCommand {
	public AlbumsOfTheDecadeCommand(DaoImplementation dao) {
		super(dao);
	}

	@Override
	public String getName() {
		return "Albums Of The Decade";
	}

	@Override
	public String getDescription() {
		return "Your top albums of the decade ";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("aotd", "albumsofthedecade");
	}

	@Override
	public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
		super.onCommand(e);
	}
}
