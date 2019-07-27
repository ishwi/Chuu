package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.NowPlayingArtist;
import DAO.Entities.UsersWrapper;
import main.Parsers.OptionalParser;
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
		this.parser = new OptionalParser("recent");
		this.respondInPrivate = false;

	}


	@Override
	public void threadableCode(MessageReceivedEvent e) {
		String[] subMessage = getSubMessage(e.getMessage());
		String[] message = parser.parse(e);
		if (message == null)
			return;

		boolean showFresh = message.length == subMessage.length;

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
							.append(username).append("](").append("https://www.last.fm/user/")
							.append(usersWrapper.getLastFMName())
							.append("): ")
							.append(nowPlayingArtist.getArtistName())
							.append(" - ").append(nowPlayingArtist.getSongName())
							.append(" | ").append(nowPlayingArtist.getAlbumName()).append("\n");//.append(" | ");

				}
		);
		if (a.length() == 0) {
			sendMessage(e, "None is listening to music at the moment UwU");
			return;
		}

		embedBuilder.setDescription(a);
		messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
	}

	@Override
	public String getDescription() {
		return ("Returns lists of all people playing music rn");
	}

	@Override
	public String getName() {
		return "Playing";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!playing");
	}


}
