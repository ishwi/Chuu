package main.Parsers;

import DAO.DaoImplementation;
import DAO.Entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class TimerFrameParser extends DaoParser {
	private final TimeFrameEnum defaultTFE;

	public TimerFrameParser(DaoImplementation dao, TimeFrameEnum defaultTFE) {
		super(dao);
		this.defaultTFE = defaultTFE;
	}

	@Override
	public String[] parse(MessageReceivedEvent e) {

		String[] message = getSubMessage(e.getMessage());
		TimeFrameEnum timeFrame = defaultTFE;
		String discordName;

		ChartParserAux auxiliar = new ChartParserAux(message);
		timeFrame = auxiliar.parseTimeframe(timeFrame);

		message = auxiliar.getMessage();

		discordName = getLastFmUsername1input(message, e.getAuthor().getIdLong(), e);
		if (discordName == null) {

			return null;
		}
		return new String[]{discordName, timeFrame.toApiFormat()};
	}



	@Override
	public List<String> getUsage(String commandName) {
		return Collections.singletonList("**" + commandName + " *[w,m,q,s,y,a]* *Username ** \n" +
				"\tIf time is not specified defaults to ALL time \n" +
				"\tIf username is not specified defaults to authors account \n\n"
		);
	}

}
