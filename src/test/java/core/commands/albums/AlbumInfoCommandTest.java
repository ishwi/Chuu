package core.commands.albums;

import core.parsers.params.ArtistAlbumParameters;
import core.util.ServiceView;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import test.commands.parsers.TestAssertion;
import test.commands.utils.MyCommandTest;
import test.runner.AssertionRunner;

import java.util.List;

class AlbumInfoCommandTest extends MyCommandTest<ArtistAlbumParameters, AlbumInfoCommand> {

    @Override
    protected AlbumInfoCommand getCommand(ServiceView dao) {
        return new AlbumInfoCommand(dao);
    }

    @Test
    void np() {
        AssertionRunner.fromCommand(command, "Not exisiting artist")
                .assertion(List.of(
                                TestAssertion.typing(),
                                TestAssertion.text(sendText -> sendText.error(o -> Assertions.assertThat(o).isEqualTo("The artist %s doesn't exist on last.fm".formatted("Not exisiting artist"))))
                        )
                );

    }
}
