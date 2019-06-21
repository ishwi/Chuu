package DAO.MusicBrainz;

import DAO.Entities.AlbumInfo;

import java.time.Year;
import java.util.List;

public interface MusicBrainzService {


	List<AlbumInfo> listOfYearReleases(List<AlbumInfo> mbiz, Year year);

	List<AlbumInfo> listOfCurrentYear(List<AlbumInfo> mbiz);

	List<AlbumInfo> findArtistByRelease(List<AlbumInfo> releaseInfo, Year year);

	List<AlbumInfo> findArtistByReleaseCurrentYear(List<AlbumInfo> releaseInfo);



}
