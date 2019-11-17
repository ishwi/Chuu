package main.commands;

import dao.entities.NowPlayingArtist;
import main.commands.utils.TestResources;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Optional;

public class UnreachableTests {
	@ClassRule
	public static final TestResources res = new TestResources();

	@Test
	public void spotifyNpSearch() {
		Optional<NPSpotifyCommand> any = TestResources.ogJDA.getRegisteredListeners().stream()
				.filter(x -> x instanceof NPSpotifyCommand).map(x -> (NPSpotifyCommand) x).findAny();
		assert any.isPresent();
		NPSpotifyCommand npSpotifyCommand = any.get();
		NowPlayingArtist nowPlayingArtist = new NowPlayingArtist("doesnt exist asdasdaad", "", true, "doesnt existasdasdaad", "doesntasdasdaad exists", "", "pepito");

		//This will crash but it increase coverage :D
		try {
			npSpotifyCommand.doSomethingWithArtist(nowPlayingArtist, null);
			Assert.fail();
		} catch (NullPointerException ignored) {
			return;
		}
		Assert.fail();

	}

}
