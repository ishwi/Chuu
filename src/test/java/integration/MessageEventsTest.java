package integration;


import core.translations.Messages;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.entities.GuildImpl;
import org.junit.jupiter.api.Test;
import test.commands.parsers.TestAssertion;
import test.commands.parsers.factories.Factory;
import test.runner.AssertionRunner;

import java.util.List;

import static core.translations.TranslationManager.m;
import static org.assertj.core.api.Assertions.assertThat;

public class MessageEventsTest implements IntegrationTest {

    @Test
    void testNonExistingPrefix() {
        Factory messageManage = Factory.def().withGuild(factoryDeps -> {
            User u = factoryDeps.user();
            assert u != null;
            GuildImpl g = new GuildImpl(factoryDeps.jda(), u.getIdLong());
            g.setOwnerId(u.getIdLong());
            return g;
        });
        AssertionRunner.fromMessage(";np", messageManage).emptyAssertion();

        AssertionRunner.fromMessage("!prefix ;", messageManage).
                assertion(List.of(TestAssertion.typing(),
                        TestAssertion.text(sendText ->
                                assertThat(sendText.output()).isEqualTo(m(Messages.PREFIX_COMMAND_SUCCESS, ';'))
                        )));

        AssertionRunner.fromMessage("!np").emptyAssertion();


    }
}
