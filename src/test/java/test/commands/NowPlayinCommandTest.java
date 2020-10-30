package test.commands;

import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.exceptions.LastFmException;
import dao.entities.NowPlayingArtist;
import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.EmbedWithFieldsUtils;
import test.commands.utils.FieldRowMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NowPlayinCommandTest extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!np";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.npParser(COMMAND_ALIAS);
	}

	@Test
	public void normalUsage() {
		Pattern compile = Pattern.compile("\\*\\*(.*?)\\*\\* - (.*?) \\| (.*)");
		ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();
		try {
			NowPlayingArtist pablopita = lastFM.getNowPlayingInfo("pablopita");
			String header = pablopita.isNowPlaying() ? "Current:" : "Last:";
			List<FieldRowMatcher> fieldRowMatchers = new ArrayList<>();
			fieldRowMatchers.add(new FieldRowMatcher(header, compile));

			EmbedWithFieldsUtils.testEmbedWithFields(COMMAND_ALIAS, null, fieldRowMatchers, Pattern
					.compile("Now Playing:"), matcher -> true);

		} catch (LastFmException e) {
			e.printStackTrace();
			System.out.println(" Last Fm Failed sorry :(");
		}

	}
}
