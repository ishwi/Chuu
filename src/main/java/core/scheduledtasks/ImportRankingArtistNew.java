package core.scheduledtasks;

import dao.ChuuService;

public record ImportRankingArtistNew(ChuuService dao) implements Runnable {

    @Override
    public void run() {
        dao.updateArtistRankingNew();
    }
}
