package DAO.MusicBrainz;

public class MusicBrainzServiceSingleton {


	private static MusicBrainzService instance;

	private MusicBrainzServiceSingleton() {
	}

	public static synchronized MusicBrainzService getInstance() {
		if (instance == null) {
			instance = new MusicBrainzServiceImpl();

		}
		return instance;
	}

}
