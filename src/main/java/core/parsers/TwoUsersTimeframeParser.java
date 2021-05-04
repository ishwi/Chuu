package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.TimeframeExplanation;
import core.parsers.explanation.TwoUsersExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.TwoUsersTimeframeParamaters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TwoUsersTimeframeParser extends DaoParser<TwoUsersTimeframeParamaters> {
    public TwoUsersTimeframeParser(ChuuService dao) {
        super(dao);
    }

    public TwoUsersTimeframeParser(ChuuService dao, OptionalEntity... opts) {
        super(dao);
        this.opts.addAll(Arrays.asList(opts));
    }

    @Override
    public TwoUsersTimeframeParamaters parseSlashLogic(ContextSlashReceived e) throws LastFmException, InstanceNotFoundException {

        SlashCommandEvent event = e.e();
        User oneUser = event.getOption(TwoUsersExplanation.NAME).getAsUser();
        TimeFrameEnum timeFrameEnum = Optional.ofNullable(e.e().getOption(TimeframeExplanation.NAME)).map(SlashCommandEvent.OptionData::getAsString).map(TimeFrameEnum::get).orElse(TimeFrameEnum.ALL);
        if (!e.isFromGuild() && oneUser.getIdLong() != e.getAuthor().getIdLong()) {
            sendError("Can't get two different users on DM's", e);
            return null;
        }
        return new TwoUsersTimeframeParamaters(e, findLastfmFromID(e.getAuthor(), e), findLastfmFromID(oneUser, e), timeFrameEnum);
    }

    public TwoUsersTimeframeParamaters parseLogic(Context e, String[] words) throws InstanceNotFoundException {
        String[] message = getSubMessage(e);

        if (message.length == 0) {
            sendError(getErrorMessage(5), e);
            return null;
        }
        ChartParserAux chartParserAux = new ChartParserAux(words);
        TimeFrameEnum timeFrameEnum = chartParserAux.parseTimeframe(TimeFrameEnum.ALL);
        words = chartParserAux.getMessage();

        ParserAux parserAux = new ParserAux(words, isExpensiveSearch());
        LastFMData[] datas = parserAux.getTwoUsers(dao, words, e);
        // words = parserAux.getMessage();
        if (datas == null) {
            sendError("Couldn't get two users", e);
            return null;
        }
        if (!e.isFromGuild() && !datas[0].getDiscordId().equals(datas[1].getDiscordId())) {
            sendError("Can't get two different users on DM's", e);
            return null;
        }
        return new TwoUsersTimeframeParamaters(e, datas[0], datas[1], timeFrameEnum);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(new TwoUsersExplanation(), new TimeframeExplanation(TimeFrameEnum.ALL));
    }


    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.put(5, "Need at least one username");
        errorMessages.put(-1, "Mentioned user is not registered");


    }
}
