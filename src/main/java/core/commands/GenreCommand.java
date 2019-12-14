package core.commands;

import dao.DaoImplementation;
import dao.entities.AlbumInfo;
import dao.entities.Genre;
import dao.entities.TimeFrameEnum;
import dao.entities.UrlCapsule;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.TimerFrameParser;
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
		this.musicBrainz = MusicBrainzServiceSingleton.getInstance();
	}

	@Override
	public String getDescription() {
		return "Genre list";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("genre");
	}

	@Override
	public String getName() {
		return "Genre";
	}

	@Override
	protected void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {

		String[] returned = parser.parse(e);
		String username = returned[0];
		long discordId = Long.parseLong(returned[1]);

		String timeframe = returned[2];
		String usableString = getUserStringConsideringGuildOrNot(e, discordId, username);
		BlockingQueue<UrlCapsule> queue = new LinkedBlockingQueue<>();
		lastFM.getUserList(username, timeframe, 15, 15, true, queue);

		List<AlbumInfo> albumInfos = queue.stream()
				.map(capsule -> new AlbumInfo(capsule.getMbid(), capsule.getAlbumName(), capsule.getArtistName()))
				.filter(u -> u.getMbid() != null && !u.getMbid().isEmpty())
				.collect(Collectors.toList());
		Map<Genre, Integer> map = musicBrainz.genreCount(albumInfos);
		if (map.isEmpty()) {
			sendMessageQueue(e, "Was not able to find any genre in  " + usableString + "'s artist");
			return;
		}

		PieChart pieChart =
				new PieChartBuilder()
						.width(800)
						.height(600)
						.title("Top 10 Genres from " + usableString + "in the last " + TimeFrameEnum
								.fromCompletePeriod(timeframe).toString())
						.theme(Styler.ChartTheme.GGPlot2)
						.build();
		pieChart.getStyler().setLegendVisible(false);
		pieChart.getStyler().setAnnotationDistance(1.15);
		pieChart.getStyler().setPlotContentSize(.7);
		pieChart.getStyler().setCircular(true);
		pieChart.getStyler().setAnnotationType(PieStyler.AnnotationType.LabelAndPercentage);
		pieChart.getStyler().setDrawAllAnnotations(true);
		pieChart.getStyler().setStartAngleInDegrees(90);

		map.entrySet().stream().sorted(((o1, o2) -> o2.getValue().compareTo(o1.getValue()))).sequential().limit(10)
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


}
