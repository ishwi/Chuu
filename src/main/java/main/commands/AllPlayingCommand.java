package main.commands;

import dao.DaoImplementation;
import dao.entities.NowPlayingArtist;
import dao.entities.UsersWrapper;
import main.exceptions.InstanceNotFoundException;
import main.exceptions.LastFmException;
import main.parsers.OptionableParser;
import main.parsers.OptionalEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AllPlayingCommand extends ConcurrentCommand {
	public AllPlayingCommand(DaoImplementation dao) {
		super(dao);

		this.parser = new OptionableParser(new OptionalEntity("--recent", "show last song from ALL users"));
		this.respondInPrivate = false;

	}

	@Override
	public String getDescription() {
		return ("Returns lists of all people playing music rn");
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("playing");
	}

	@Override
	public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {

		String[] message = parser.parse(e);
		boolean showFresh = !Boolean.parseBoolean(message[0]);

		List<UsersWrapper> list = getDao().getAll(e.getGuild().getIdLong());
		MessageBuilder messageBuilder = new MessageBuilder();

		EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
				.setThumbnail(e.getGuild().getIconUrl())
				.setTitle("What is being played now in " + e.getGuild().getName());
		StringBuilder a = new StringBuilder();

		Map<UsersWrapper, Optional<NowPlayingArtist>> npList = list.parallelStream().
				collect(Collectors.toConcurrentMap(u -> u, uw ->

				{
					try {
						return Optional.of(lastFM.getNowPlayingInfo(uw.getLastFMName()));
					} catch (Exception ex) {
						return Optional.empty();
					}
				}));

		npList.forEach((usersWrapper, optionalNowPlayingArtist) -> {
					if (!optionalNowPlayingArtist.isPresent())
						return;
					NowPlayingArtist nowPlayingArtist = optionalNowPlayingArtist.get();
					if (showFresh) {

						if (!nowPlayingArtist.isNowPlaying()) {
							return;
						}
					}

					Member member = e.getGuild().getMemberById(usersWrapper.getDiscordID());

					String username = member == null ? usersWrapper.getLastFMName() : member.getEffectiveName();

					a.append("+ ").append("[")
							.append(username).append("](")
							.append(CommandUtil.getLastFmUser(usersWrapper.getLastFMName()))
							.append("): ")
							.append(nowPlayingArtist.getSongName())
							.append(" - ").append(nowPlayingArtist.getArtistName())
							.append(" | ").append(nowPlayingArtist.getAlbumName()).append("\n");//.append(" | ");

				}
		);
		if (a.length() == 0) {
			sendMessageQueue(e, "None is listening to music at the moment UwU");
			return;
		}

		embedBuilder.setDescription(a);
		messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
	}

	@Override
	public String getName() {
		return "Playing";
	}


}
