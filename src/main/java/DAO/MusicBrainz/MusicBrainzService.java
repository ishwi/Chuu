package DAO.MusicBrainz;

import DAO.Entities.AlbumInfo;
import DAO.Entities.AlbumUserPlays;
import DAO.Entities.ArtistData;

import java.time.Year;
import java.util.List;

public interface MusicBrainzService {


	List<AlbumInfo> listOfYearReleases(List<AlbumInfo> mbiz, Year year);

	List<AlbumInfo> listOfCurrentYear(List<AlbumInfo> mbiz);

	List<ArtistData> findArtistByName(String name);

	boolean isReleaseFromCurrentYear(AlbumUserPlays albumUserPlays);

	List<AlbumUserPlays> gindReleaseByMbiz(String mbiz);

	List<AlbumUserPlays> findReleaseByName(String name);


}
