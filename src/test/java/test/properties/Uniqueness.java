package test.properties;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import dao.entities.ReturnNowPlaying;
import dao.entities.UniqueData;
import dao.entities.WrapperReturnNowPlaying;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import test.commands.utils.TestResources;

import java.util.List;

@RunWith(JUnitQuickcheck.class)
public class Uniqueness {
	@ClassRule
	public static final TestResources res = new TestResources();

	@Property
	public void onlyOneUser(@From(UniqueGen.class) UniqueData uniqueData) {


		WrapperReturnNowPlaying wrapperReturnNowPlaying = TestResources.dao
				.whoKnows(uniqueData.getArtistName(), TestResources.channelWorker.getGuild()
						.getIdLong(), Integer.MAX_VALUE);

		System.out.println(uniqueData.getArtistName());
		List<ReturnNowPlaying> returnNowPlayings = wrapperReturnNowPlaying.getReturnNowPlayings();
		Assert.assertEquals(1, returnNowPlayings.size());
		Assert.assertEquals(returnNowPlayings.get(0).getLastFMId(), "pablopita");

	}


}
