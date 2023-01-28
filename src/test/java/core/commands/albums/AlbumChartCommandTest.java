package core.commands.albums;

import core.commands.charts.AlbumChartCommand;
import core.parsers.params.ChartParameters;
import core.util.ServiceView;
import org.junit.jupiter.api.Test;
import test.commands.parsers.TestAssertion;
import test.commands.utils.ImageUtils2;
import test.commands.utils.MyCommandTest;
import test.runner.AssertionRunner;

import java.util.List;

class AlbumChartCommandTest extends MyCommandTest<ChartParameters, AlbumChartCommand> {

    @Override
    protected AlbumChartCommand getCommand(ServiceView dao) {
        return new AlbumChartCommand(dao);
    }

    @Test
    public void ChartNormalTest() {

        AssertionRunner.fromCommand(command, "a 1x1")
                .assertion(List.of(
                        TestAssertion.typing(),
                        TestAssertion.image(
                                (e) -> ImageUtils2.testImage(e, 300, 300, ".png"),
                                "Then I should receive a 300x300 png image"
                        )));

    }

    @Test
    public void ChartBigTest() {

        AssertionRunner.fromCommand(command, "a 20x11")
                .assertion(
                        List.of(
                                TestAssertion.typing(),
                                TestAssertion.image(
                                        (e) -> ImageUtils2.testImage(e, 3300, 1650, ".jpg"),
                                        "Then I should receive a 900x1500 jpg image"
                                )));
    }
}
