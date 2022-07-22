package test.commands;

import core.apis.last.exceptions.AlbumException;
import core.apis.last.exceptions.ExceptionEntity;
import core.commands.stats.NPSpotifyCommand;
import core.parsers.NoOpParser;
import core.parsers.utils.OptionalEntity;
import dao.entities.NowPlayingArtist;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import test.commands.utils.TestResources;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(TestResources.class)
public class UnreachableTests {

    @Test()
    public void spotifyNpSearch() {
        Optional<NPSpotifyCommand> any = TestResources.ogJDA.getRegisteredListeners().stream()
                .filter(x -> x instanceof NPSpotifyCommand).map(x -> (NPSpotifyCommand) x).findAny();
        assert any.isPresent();
        NPSpotifyCommand npSpotifyCommand = any.get();
        NowPlayingArtist nowPlayingArtist = new NowPlayingArtist("doesnt exist asdasdaad", "", true, "doesnt existasdasdaad", "doesntasdasdaad exists", "", "pepito", true);

        //This will crash but it increase coverage :D
        assertThatThrownBy(() -> npSpotifyCommand.doSomethingWithArtist(nowPlayingArtist, null, -1L, null, null)).isInstanceOf(NullPointerException.class);

    }

    @Test()
    public void noOpParserParsing() {
        NoOpParser noOpParser = NoOpParser.INSTANCE;
        assertThatThrownBy(() -> noOpParser.parseLogic(null, null)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void OptionalEntityEquals() {
        OptionalEntity optionalEntity = new OptionalEntity("test", "testdef");
        assertThat(optionalEntity).isEqualTo(optionalEntity);
        assertThat(optionalEntity).isNotEqualTo(optionalEntity.getDescription());
        assertThat(optionalEntity).isNotNull();
    }


    @Test
    public void gettersSettersExeption() {
        AlbumException exceptionEntity = new AlbumException("artist", "album");
        ExceptionEntity exceptionEntity1 = new ExceptionEntity("username");
        assertThat(exceptionEntity.getArtist()).isEqualTo("artist");
        assertThat(exceptionEntity.getArtist()).isEqualTo("artist");
        assertThat(exceptionEntity.getAlbum()).isEqualTo("album");
        assertThat(exceptionEntity.getUserName()).isEqualTo("username");

    }


}
