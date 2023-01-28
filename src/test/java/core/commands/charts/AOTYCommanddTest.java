package core.commands.charts;

import core.commands.NullReturnParsersTest;
import core.parsers.params.ChartYearParameters;
import core.util.ServiceView;
import org.junit.jupiter.api.Test;
import test.commands.parsers.TestAssertion;
import test.commands.utils.MyCommandTest;
import test.runner.AssertionRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class AOTYCommanddTest extends MyCommandTest<ChartYearParameters, AOTYCommand> {


    @Test
    public void nullParserReturned() {
        NullReturnParsersTest.chartFromYearParser(command, " ");
    }

    @Test
    public void notFound() {
        Pattern compile = Pattern.compile("Dont have any (\\d{4}) album in your top (\\d+) albums");

        AssertionRunner.fromCommand(command, "a 1876").assertion(List.of(
                TestAssertion.typing(),
                TestAssertion.text(txt -> {
                    Matcher matcher = compile.matcher(txt.output());
                    assertThat(matcher.matches()).isTrue();
                    assertThat(matcher.group(1)).isEqualTo("1876");
                    assertThat(matcher.group(2)).isEqualTo("150");
                })
        ));
    }

    @Test
    public void normalWorking() {
        AssertionRunner.fromCommand(command, "2018 1x1").assertion(List.of(
                TestAssertion.typing(),
                TestAssertion.image(img -> {
                    BufferedImage image = ImageIO.read(img.io());
                    assertThat(img.filename()).endsWith(".png");
                    assertThat(image.getWidth()).isEqualTo(300);
                    assertThat(image.getHeight()).isEqualTo(300);
                })
        ));
    }

    @Test
    public void failingCases() {
        AssertionRunner.fromCommand(command, "1x1 --nolimit").assertion(List.of(
                TestAssertion.typing(),
                TestAssertion.error(charSequence -> assertThat(charSequence).isEqualTo("Cant use a size for the chart if you specify the --nolimit flag")))
        );

        AssertionRunner.fromCommand(command, "1x0").assertion(List.of(
                TestAssertion.typing(),
                TestAssertion.error(charSequence -> assertThat(charSequence).isEqualTo("0 is not a valid value for a chart!")))
        );
    }

    @Override
    protected AOTYCommand getCommand(ServiceView dao) {
        return new AOTYCommand(dao);
    }
}
