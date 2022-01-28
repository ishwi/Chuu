package core.scheduledtasks;

import dao.ChuuService;

public record ImportRankingArtist(ChuuService dao) implements Runnable {

    @Override
    public void run() {
        dao.updateArtistRanking();
    }
}
