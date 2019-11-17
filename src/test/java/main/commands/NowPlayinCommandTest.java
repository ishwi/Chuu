package main.commands;

import dao.entities.NowPlayingArtist;
import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import main.commands.utils.EmbedWithFieldsUtils;
import main.commands.utils.FieldRowMatcher;
import main.commands.utils.TestResources;
import main.exceptions.LastFmException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
		Optional<ConcurrentCommand> any = TestResources.ogJDA.getRegisteredListeners().stream()
				.filter(x -> x instanceof ConcurrentCommand).map(x -> (ConcurrentCommand) x).findAny();
		assert any.isPresent();
		ConcurrentCommand myCommand = any.get();

		try {
			NowPlayingArtist pablopita = myCommand.lastFM.getNowPlayingInfo("pablopita");
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
