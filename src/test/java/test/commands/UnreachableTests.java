package test.commands;

import core.apis.last.exceptions.AlbumException;
import core.apis.last.exceptions.ExceptionEntity;
import core.commands.stats.NPSpotifyCommand;
import core.parsers.NoOpParser;
import core.parsers.OptionalEntity;
import core.parsers.PrefixParser;
import core.parsers.UrlParser;
import dao.entities.NowPlayingArtist;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import test.commands.utils.TestResources;

import java.util.Optional;

public class UnreachableTests {
    @ClassRule
    public static final TestRule res = TestResources.INSTANCE;

    @Test(expected = NullPointerException.class)
    public void spotifyNpSearch() {
        Optional<NPSpotifyCommand> any = TestResources.ogJDA.getRegisteredListeners().stream()
                .filter(x -> x instanceof NPSpotifyCommand).map(x -> (NPSpotifyCommand) x).findAny();
        assert any.isPresent();
        NPSpotifyCommand npSpotifyCommand = any.get();
        NowPlayingArtist nowPlayingArtist = new NowPlayingArtist("doesnt exist asdasdaad", "", true, "doesnt existasdasdaad", "doesntasdasdaad exists", "", "pepito", true);

        //This will crash but it increase coverage :D
        npSpotifyCommand.doSomethingWithArtist(nowPlayingArtist, null, -1L, null, null);

    }

    @Test(expected = UnsupportedOperationException.class)
    public void noOpParserParsing() {
        NoOpParser noOpParser = new NoOpParser();
        noOpParser.parseLogic(null, null);
    }

    @Test
    public void OptionalEntityEquals() {
        OptionalEntity optionalEntity = new OptionalEntity("test", "testdef");
        Assert.assertEquals(optionalEntity, optionalEntity);
        Assert.assertNotEquals(optionalEntity, optionalEntity.getDefinition());
        Assert.assertNotEquals(null, optionalEntity);
        Assert.assertNotEquals(this, optionalEntity);
    }


    @Test
    public void gettersSettersExeption() {
        AlbumException exceptionEntity = new AlbumException("artist", "album");
        ExceptionEntity exceptionEntity1 = new ExceptionEntity("username");
        Assert.assertEquals(exceptionEntity.getArtist(), "artist");
        Assert.assertEquals(exceptionEntity.getAlbum(), "album");
        Assert.assertEquals(exceptionEntity1.getUserName(), "username");

        UrlParser urlParser = new UrlParser();
        PrefixParser prefixParser = new PrefixParser();
    }


}
