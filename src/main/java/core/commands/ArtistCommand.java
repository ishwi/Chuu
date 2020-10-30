package core.commands;

import core.parsers.ChartParser;
import core.parsers.ChartableParser;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.Arrays;
import java.util.List;

public class ArtistCommand extends ArtistAbleCommand<ChartParameters> {

    public ArtistCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public ChartableParser<ChartParameters> initParser() {
        return new ChartParser(getService());
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartParameters params, int count) {
        return params.initEmbed("'s top artists", embedBuilder, " has listened to " + count + " artists", params.getLastfmID());
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartParameters params, int count, String initTitle) {
        String displayString = params.getTimeFrameEnum().getDisplayString();
        pieChart.setTitle(initTitle + "'s top artists" + displayString);
        return String.format("%s has listened to %d artists%s (showing top %d)", initTitle, count, displayString, params.getX() * params.getY());

    }

    @Override
    public String getDescription() {
        return "Returns a chart with artist images";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("artchart", "charta", "artistchart", "ca");
    }


    @Override
    public String getName() {
        return "Artist Chart";
    }


    @Override
    public void noElementsMessage(ChartParameters parameters) {
        MessageReceivedEvent e = parameters.getE();

        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any artist%s!", ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }

}
