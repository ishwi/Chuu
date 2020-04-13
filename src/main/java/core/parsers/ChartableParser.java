package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class ChartableParser<T extends ChartParameters> extends DaoParser<T> {
    final TimeFrameEnum defaultTFE;

    public ChartableParser(ChuuService dao, TimeFrameEnum defaultTFE) {
        super(dao);
        this.defaultTFE = defaultTFE;
    }


    public ChartableParser(ChuuService dao, TimeFrameEnum defaultTFE, OptionalEntity... opts) {
        super(dao, opts);
        this.defaultTFE = defaultTFE;
    }

    @Override
    void setUpOptionals() {
        opts.add(new OptionalEntity("--notitles", "dont display titles"));
        opts.add(new OptionalEntity("--plays", "display play count"));
        opts.add(new OptionalEntity("--list", "display it as an embed"));
        opts.add(new OptionalEntity("--pie", "display it as a chart pie"));

    }

    @Override
    public abstract T parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException;


    @Override
    public String getErrorMessage(int code) {
        return errorMessages.get(code);
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *[w,m,q,s,y,a]* *sizeXsize*  *Username* ** \n" +
               "\tOriginal tool: http://thechurchofkoen.com/" +
               "\tIf time is not specified defaults to " + defaultTFE.name().toLowerCase() + "\n" +
               "\tIf username is not specified defaults to authors account \n" +
               "\tIf Size not specified it defaults to 5x5\n";
    }

    @Override
    protected void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.put(5, "You Introduced too many words");
        errorMessages.put(6, "Invalid number for the size of the chart!");
        errorMessages.put(7, "Can't introduce a size if you are doing list mode");
    }

}
