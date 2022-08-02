package dao.musicbrainz;

public class MusicBrainzServiceSingleton {


    private static MusicBrainzService instance;

    private MusicBrainzServiceSingleton() {
    }

    public static synchronized MusicBrainzService getInstance() {
        if (instance == null) {
            instance = new MusicBrainzServiceImpl();
//            instance = new EmptyMusicBrainzServiceImpl();

        }
        return instance;
    }

}
