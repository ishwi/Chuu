package core.parsers;

import core.exceptions.LastFmException;
import core.parsers.explanation.ChartSizeExplanation;
import core.parsers.explanation.FullTimeframeExplanation;
import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public abstract class ChartableParser<T extends ChartParameters> extends DaoParser<T> {
    public static final int DEFAULT_X = 5;
    public static final int DEFAULT_Y = 5;
    final TimeFrameEnum defaultTFE;

    public ChartableParser(ChuuService dao, TimeFrameEnum defaultTFE) {
        super(dao);
        this.defaultTFE = defaultTFE;
        this.setExpensiveSearch(true);

    }


    public ChartableParser(ChuuService dao, TimeFrameEnum defaultTFE, OptionalEntity... opts) {
        super(dao, opts);
        this.defaultTFE = defaultTFE;
        this.setExpensiveSearch(true);
    }

    @Override
    void setUpOptionals() {
        opts.add(new OptionalEntity("notitles", "dont display titles"));
        opts.add(new OptionalEntity("plays", "display play count"));
        opts.add(new OptionalEntity("list", "display it as an embed"));
        opts.add(new OptionalEntity("pie", "display it as a pie chart"));
        opts.add(new OptionalEntity("aside", "show titles on the side"));
    }


    @Override
    public abstract T parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException;

    @Override
    public T parse(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        T params = super.parse(e);
        if (params != null) {
            if (params.getX() == DEFAULT_X && params.getY() == DEFAULT_Y) {
                String[] subMessage = getSubMessage(e.getMessage());
                if (Arrays.stream(subMessage).filter(ChartParserAux.chartSizePattern.asMatchPredicate()).findAny().isEmpty()) {
                    params.setX(params.getUser().getDefaultX());
                    params.setY(params.getUser().getDefaultY());
                }
            }
        }
        return params;
    }

    @Override
    public String getErrorMessage(int code) {
        return errorMessages.get(code);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(new FullTimeframeExplanation(defaultTFE), new ChartSizeExplanation(), new PermissiveUserExplanation());
    }


    @Override
    protected void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.put(5, "You Introduced too many words");
        errorMessages.put(6, "Chart size must be above 1 and below 400(20x20)!");
    }

}
