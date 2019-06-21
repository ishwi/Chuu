package main;

import DAO.MusicBrainz.MusicBrainzService;
import DAO.MusicBrainz.MusicBrainzServiceImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MbTest {

	@Test
	public void test1() {

		MusicBrainzService a = new MusicBrainzServiceImpl();
		List<String> mbizList = new ArrayList<>();
		mbizList.add("6a13f9bb-ffde-4593-aa1e-97f7154019a7");
		mbizList.add("2753d2b6-d36b-4879-a53e-8d474190fe28");
		mbizList.add("2c5274b5-c468-4378-8347-8d270635d06d");
		mbizList.add("de0f9f4c-a154-4f0c-8775-d389ba1c2a8e");
		mbizList.add("1adcbb60-160d-4594-ac05-f877655d085f");
		mbizList.add("316f9a76-a401-45f6-9a48-22e1bf84c1a4");
		mbizList.add("3d9cc4be-98f3-4007-a7ef-16bfdfc3c177");
//	List<String> b = a.listOfYearReleases(mbizList, Year.of(2018));
//		List<String> c = a.listOfYearReleases(mbizList, Year.of(2017));


	}
}
