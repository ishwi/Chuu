package dao.musicbrainz;

import dao.entities.AlbumInfo;
import dao.entities.FullAlbumEntity;
import dao.entities.Genre;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.exceptions.LastFmException;
import org.junit.Test;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MbTest {

	@Test
	public void test1() {

		MusicBrainzService a = MusicBrainzServiceSingleton.getInstance();
		List<AlbumInfo> mbizList = generateListMbiz();
		List<AlbumInfo> b = a.listOfYearReleases(mbizList, Year.of(2018));
		List<AlbumInfo> c = a.listOfYearReleases(mbizList, Year.of(2017));


	}

	private List<AlbumInfo> generateListMbiz() {
		List<AlbumInfo> mbizList = new ArrayList<>();
		mbizList.add(new AlbumInfo("6a13f9bb-ffde-4593-aa1e-97f7154019a7", "", ""));
		mbizList.add(new AlbumInfo("2753d2b6-d36b-4879-a53e-8d474190fe28", "", ""));
		mbizList.add(new AlbumInfo("2c5274b5-c468-4378-8347-8d270635d06d", "", ""));
		mbizList.add(new AlbumInfo("de0f9f4c-a154-4f0c-8775-d389ba1c2a8e", "", ""));
		mbizList.add(new AlbumInfo("1adcbb60-160d-4594-ac05-f877655d085f", "", ""));
		mbizList.add(new AlbumInfo("316f9a76-a401-45f6-9a48-22e1bf84c1a4", "", ""));
		mbizList.add(new AlbumInfo("3d9cc4be-98f3-4007-a7ef-16bfdfc3c177", "", ""));
		return mbizList;
	}

	@Test
	public void test2() {

		MusicBrainzService a = MusicBrainzServiceSingleton.getInstance();
		List<AlbumInfo> mbizList = new ArrayList<>();
		mbizList.add(new AlbumInfo("", "Prophets", "counterparts"));
		mbizList.add(new AlbumInfo("", "Wildlife", "La dispute"));
		mbizList.add(new AlbumInfo("", "Tiny Dots", "La dispute"));
		List<AlbumInfo> b = a.findArtistByRelease(mbizList, Year.of(2016));
//		List<String> c = a.listOfYearReleases(mbizList, Year.of(2017));

	}

	@Test
	public void test3() {

		MusicBrainzService a = MusicBrainzServiceSingleton.getInstance();
		List<AlbumInfo> mbizList = generateListMbiz();

		Map<Genre, Integer> map = a.genreCount(mbizList);
		StringBuilder sb = new StringBuilder();
		map.entrySet().stream().sorted(((o1, o2) -> -o1.getValue().compareTo(o2.getValue()))).forEachOrdered(entry -> {
			Genre genre = entry.getKey();
			int plays = entry.getValue();
			sb.append("Genre: ").append(genre.getGenreName()).append(" \n")
					.append("Frequency: ").append(plays).append("\n").append("Representative ")
					.append(genre.getRepresentativeArtist()).append("\n");
		});
		System.out.println(sb.toString());
//		List<String> c = a.listOfYearReleases(mbizList, Year.of(2017));

	}

	@Test
	public void test4() throws LastFmException {

		MusicBrainzService a = MusicBrainzServiceSingleton.getInstance();
		ConcurrentLastFM lastFM = LastFMFactory.getNewInstance();

		FullAlbumEntity albumTrackListLowerCase = lastFM
				.getTracksAlbum("ishwaracoello", "BROCKHAMPTON", "SATURATION III");

		//	List<String> c = a.listOfYearReleases(mbizList, Year.of(2017));

	}
}
