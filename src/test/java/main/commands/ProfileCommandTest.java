package main.commands;

import dao.entities.UserInfo;
import main.apis.last.ConcurrentLastFM;
import main.apis.last.LastFMFactory;
import main.commands.parsers.NullReturnParsersTest;
import main.commands.utils.CommandTest;
import main.commands.utils.GenericEmbedMatcher;
import main.commands.utils.ImageUtils;
import main.commands.utils.TestResources;
import main.exceptions.LastFmException;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileCommandTest extends CommandTest {
	@Override
	public String giveCommandName() {
		return "!profile";
	}

	@Test
	@Override
	public void nullParserReturned() {
		NullReturnParsersTest.onlyUsernameParser(COMMAND_ALIAS);
	}

	@Test
	public void normalUsageEmbedded() throws LastFmException {

		Pattern footerRegex = Pattern
				.compile("Account created on (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})");
		Predicate<Matcher> matcherFooter = matcher1 -> {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD HH:mm");
				sdf.parse((matcher1.group(1)));
				return true;
			} catch (ParseException ex) {
				return false;
			}
		};
		Pattern descriptionRegex = Pattern
				.compile("(?:Total number of (scrobbles|albums|artists|crowns|unique artist): (\\d+)|Top (crown|unique):(.*))");
		Pattern titleRegex = Pattern.compile("(.*)'s profile");
		Predicate<Matcher> matcherBooleanFunction = (Matcher matcher) -> matcher
				.group(1).equals(TestResources.testerJdaUsername);

		ConcurrentLastFM newInstance = LastFMFactory.getNewInstance();
		List<UserInfo> pablopita = newInstance.getUserInfo(Collections.singletonList("pablopita"));
		String image = pablopita.get(0).getImage();
		image = image.isEmpty() ? null : image;
		GenericEmbedMatcher
				.GeneralFunction(COMMAND_ALIAS, footerRegex, matcherFooter, titleRegex, matcherBooleanFunction, descriptionRegex, null, null, null, 45, true, image, null);


	}

	@Test
	public void normalUsageImage() throws LastFmException {

		ImageUtils.testImage(COMMAND_ALIAS + " --image", 1080, 1920, 50, ".png");
	}
}
