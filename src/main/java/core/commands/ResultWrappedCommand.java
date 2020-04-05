package core.commands;

import core.commands.util.PieableResultWrapper;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.imagerenderer.GraphicUtils;
import core.parsers.OptionableParser;
import core.parsers.OptionalEntity;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.ResultWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

public abstract class ResultWrappedCommand<T, Y extends CommandParameters> extends ConcurrentCommand {
    public PieableResultWrapper<T, Y> pie;

    ResultWrappedCommand(ChuuService dao) {
        super(dao);
        this.parser = new OptionableParser(new OptionalEntity("--pie", "display it as a chart pie"));
        this.pie = null;
    }

    public abstract Y getParameters(MessageReceivedEvent e, String[] message);

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] parse = this.parser.parse(e);
        if (parse == null) {
            return;
        }
        Y parameters = getParameters(e, parse);
        if (parameters.hasOptional("--pie")) {
            doPie(getList(parse, e), e, parameters);
            return;
        }
        printList(getList(parse, e), e, parameters);
    }

    private void doPie(ResultWrapper<T> list, MessageReceivedEvent e, Y parameters) {
        PieChart pieChart = this.pie.doPie(parameters, list.getResultList());
        doPie(pieChart, parameters, list.getRows());

    }

    public EmbedBuilder initList(List<String> collect) {
        StringBuilder a = new StringBuilder();
        for (int i = 0, size = collect.size(); i < 10 && i < size; i++) {
            String text = collect.get(i);
            a.append(i + 1).append(text);
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(a);
        return embedBuilder.setColor(CommandUtil.randomColor());
    }

    public void doPie(PieChart pieChart, Y chartParameters, int count) {
        BufferedImage bufferedImage = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        String s = fillPie(pieChart, chartParameters, count);

        GraphicUtils.setQuality(g);
        pieChart.paint(g, 1000, 750);

        Font annotationsFont = pieChart.getStyler().getAnnotationsFont();
        pieChart.paint(g, 1000, 750);
        g.setFont(annotationsFont.deriveFont(11.0f));
        Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(s, g);
        g.drawString(s, 1000 - 10 - (int) stringBounds.getWidth(), 740 - 2);
        sendImage(bufferedImage, chartParameters.getE());
    }

    protected abstract String fillPie(PieChart pieChart, Y params, int count);

    public abstract ResultWrapper<T> getList(String[] message, MessageReceivedEvent e) throws LastFmException;

    public abstract void printList(ResultWrapper<T> list, MessageReceivedEvent e, CommandParameters commandParameters);

}
