package dao;

public record ServiceView(ChuuService normalService, ChuuService longService) {

    public ChuuService getView(boolean isLongRunningCommand) {
        if (isLongRunningCommand) {
            return longService;
        }
        return normalService;
    }
}
