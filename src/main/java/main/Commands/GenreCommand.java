package main.Commands;

import DAO.DaoImplementation;
import DAO.Entities.AlbumInfo;
import DAO.Entities.Genre;
import DAO.Entities.TimeFrameEnum;
import DAO.Entities.UrlCapsule;
import DAO.MusicBrainz.MusicBrainzService;
import DAO.MusicBrainz.MusicBrainzServiceImpl;
import main.Exceptions.LastFmException;
import main.Parsers.TimerFrameParser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class GenreCommand extends ConcurrentCommand {
	private final MusicBrainzService musicBrainz;

	public GenreCommand(DaoImplementation dao) {
		super(dao);
		this.parser = new TimerFrameParser(dao, TimeFrameEnum.YEAR);
		this.musicBrainz = new MusicBrainzServiceImpl();
	}

	@Override
	protected void threadableCode(MessageReceivedEvent e) {

		String[] returned;
		returned = parser.parse(e);
		if (returned == null)
			return;

		String username = returned[0];
		String timeframe = returned[1];

		BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();
		try {
			lastFM.getUserList(username, timeframe, 15, 15, true, queue);
		} catch (LastFmException ex) {
			ex.printStackTrace();
			return;
		}
		PieChart pieChart =
				new PieChartBuilder()
						.width(800)
						.height(600)
						.title("Top 10 Genres in the last " + TimeFrameEnum.fromCompletePeriod(timeframe).toString())
						.theme(Styler.ChartTheme.GGPlot2)
						.build();
		pieChart.getStyler().setLegendVisible(false);
		pieChart.getStyler().setAnnotationDistance(1.15);
		pieChart.getStyler().setPlotContentSize(.7);
		pieChart.getStyler().setCircular(true);
		pieChart.getStyler().setAnnotationType(PieStyler.AnnotationType.LabelAndPercentage);
		pieChart.getStyler().setDrawAllAnnotations(true);
		pieChart.getStyler().setStartAngleInDegrees(90);
		List<AlbumInfo> albumInfos = queue.stream()
				.map(capsule -> new AlbumInfo(capsule.getMbid(), capsule.getAlbumName(), capsule.getArtistName()))
				.collect(Collectors.toList());
		List<AlbumInfo> mbizList =
				albumInfos.stream().filter(u -> u.getMbid() != null && !u.getMbid().isEmpty())
						.collect(Collectors.toList());
		Map<Genre, Integer> map = musicBrainz.genreCount(mbizList);
		map.entrySet().stream().sorted(((o1, o2) -> -o1.getValue().compareTo(o2.getValue()))).sequential().limit(10)
				.forEachOrdered(entry -> {
					Genre genre = entry.getKey();
					int plays = entry.getValue();
					pieChart.addSeries(genre.getGenreName(), plays);
				});
		BufferedImage bufferedImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bufferedImage.createGraphics();
		pieChart.paint(g, 800, 600);
		sendImage(bufferedImage, e);

	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("!genre");
	}

	@Override
	public String getDescription() {
		return "genre list";
	}

	@Override
	public String getName() {
		return "Genre";
	}


}
