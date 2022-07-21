package core.util;

import core.commands.abstracts.MyCommand;
import core.commands.config.SetCommand;
import core.commands.config.UnsetCommand;
import core.commands.config.UpdateCommand;
import core.commands.scrobble.LoginCommand;
import dao.ChuuService;

public record ServiceView(ChuuService normalService, ChuuService longService, ChuuService updaterService) {

    private ChuuService getView(boolean isLongRunningCommand) {
        if (isLongRunningCommand) {
            return longService;
        }
        return normalService;
    }

    public ChuuService getView(boolean isLongRunningCommand, MyCommand<?> tMyCommand) {
        return switch (tMyCommand) {
            case UpdateCommand c -> updaterService;
            case SetCommand c -> updaterService;
            case LoginCommand c -> updaterService;
            case UnsetCommand c -> updaterService;
            default -> getView(isLongRunningCommand);
        };
    }
}
