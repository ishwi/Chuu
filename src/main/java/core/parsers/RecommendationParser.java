package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.params.RecommendationsParams;
import dao.ChuuService;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RecommendationParser extends DaoParser<RecommendationsParams> {
    public RecommendationParser(ChuuService dao) {
        super(dao);
    }

    @Override
    void setUpOptionals() {
        this.opts.add(new OptionalEntity("--repeated", "gives you a repeated recommendation"));
    }

    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
    }

    @Override
    public RecommendationsParams parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException {

        ParserAux parserAux = new ParserAux(words);
        LastFMData[] datas = parserAux.getTwoUsers(dao, words, e);
        boolean noUserFlag = false;
        if (datas == null) {
            noUserFlag = true;
        } else if (datas[0].getDiscordId().equals(datas[1].getDiscordId())) {
            e.getChannel().sendMessage("Dont't use the same person twice\n").queue();
            return null;
        }
        if (noUserFlag) {
            return new RecommendationsParams(e, null, null, true);
        } else {
            return new RecommendationsParams(e, datas[0], datas[1], false);
        }

    }


    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *user* **\n" +
               "\t If user is not specified if will give you a recommendation from your closest match, otherwise a rec from that user" +
               "\t Alternatively you could also mention two different users\n\n";

    }
}
