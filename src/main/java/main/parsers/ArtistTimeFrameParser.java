package main.parsers;

import dao.DaoImplementation;
import dao.entities.NowPlayingArtist;
import dao.entities.TimeFrameEnum;
import main.apis.last.ConcurrentLastFM;
import main.exceptions.LastFmException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.ArrayUtils;

import javax.management.InstanceNotFoundException;
import java.util.Arrays;
import java.util.List;

public class ArtistTimeFrameParser extends ArtistParser {
	private final TimeFrameEnum defaultTFE = TimeFrameEnum.ALL;

	public ArtistTimeFrameParser(DaoImplementation dao, ConcurrentLastFM lastFM) {
		super(dao, lastFM);
	}

	@Override
	public String[] parseLogic(MessageReceivedEvent e, String[] words) {
		User sample;
		TimeFrameEnum timeFrame = defaultTFE;

		ChartParserAux chartParserAux = new ChartParserAux(words);
		timeFrame = chartParserAux.parseTimeframe(timeFrame);
		words = chartParserAux.getMessage();
		String[] strings;
		if (e.isFromGuild()) {
			List<Member> members = e.getMessage().getMentionedMembers();
			if (!members.isEmpty()) {
				if (members.size() != 1) {
					sendError("Only one user pls", e);
					return null;
				}
				sample = members.get(0).getUser();
				words = Arrays.stream(words).filter(s -> !s.equals(sample.getAsMention()))
						.toArray(String[]::new);
			} else {
				sample = e.getMember().getUser();
			}
		} else
			sample = e.getAuthor();

		if (words.length == 0) {

			NowPlayingArtist np;
			try {

				String userName = dao.findLastFMData(sample.getIdLong()).getName();
				np = lastFM.getNowPlayingInfo(userName);

			} catch (InstanceNotFoundException ex) {
				sendError(sample.getName() + " needs to be registered on the bot!", e);
				return null;
			} catch (LastFmException ex) {
				sendError(this.getErrorMessage(2), e);
				return null;
			}

			strings = doSomethingWithNp(np, sample, e);

		} else {

			strings = doSomethingWithString(words, sample, e);
		}
		return ArrayUtils.add(strings, timeFrame.toApiFormat());

	}

	@Override
	public String getUsageLogic(String commandName) {
		return "**" + commandName + " *artist* *[w,m,q,s,y,a]*** \n" +
				"\tIf time is not specified defaults to Yearly \n";
	}
}
