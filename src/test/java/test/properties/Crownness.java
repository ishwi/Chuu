package test.properties;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import dao.entities.ReturnNowPlaying;
import dao.entities.UniqueData;
import dao.entities.WrapperReturnNowPlaying;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import test.commands.utils.TestResources;

import java.util.List;

@RunWith(JUnitQuickcheck.class)
public class Crownness {
	@ClassRule

	public static final TestRule res = TestResources.INSTANCE;

	@Property
	public void youAreFirst(@From(CrownGen.class) UniqueData uniqueData) {

		//Who knows the given artist
		WrapperReturnNowPlaying wrapperReturnNowPlaying = TestResources.dao
				.whoKnows(uniqueData.getArtistName(), TestResources.channelWorker.getGuild()
						.getIdLong(), Integer.MAX_VALUE);

		System.out.println(uniqueData.getArtistName());

		List<ReturnNowPlaying> returnNowPlayings = wrapperReturnNowPlaying.getReturnNowPlayings();
		//We know there is at least one
		Assert.assertTrue(returnNowPlayings.size() >= 1);
		//we should be the first one
		Assert.assertEquals(returnNowPlayings.get(0).getLastFMId(), "pablopita");

	}
}
