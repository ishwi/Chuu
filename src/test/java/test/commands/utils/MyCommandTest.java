package test.commands.utils;

import core.commands.abstracts.MyCommand;
import core.parsers.params.CommandParameters;
import core.util.ServiceView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestResources.class)
public abstract class MyCommandTest<Y extends CommandParameters, T extends MyCommand<Y>> {

    protected T command;

    @BeforeEach
    void beforeAll() {
        command = getCommand(new ServiceView(TestResources.dao, TestResources.dao, TestResources.dao));
    }


    protected abstract T getCommand(ServiceView dao);
}
