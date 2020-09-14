package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.params.RecommendationsParams;
import dao.ChuuService;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class RecommendationParser extends DaoParser<RecommendationsParams> {
    private final int defaultCount;

    public RecommendationParser(ChuuService dao) {
        this(dao, 1);
    }

    public RecommendationParser(ChuuService dao, int defaultCount) {
        super(dao);
        this.defaultCount = defaultCount;
    }

    public int getDefaultCount() {
        return defaultCount;
    }

    @Override
    void setUpOptionals() {
        this.opts.add(new OptionalEntity("repeated", "gives you a repeated recommendation"));
    }

    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
    }

    @Override
    public RecommendationsParams parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException {

        Stream<String> secondStream = Arrays.stream(words).filter(s -> s.matches("\\d+"));
        Optional<String> opt2 = secondStream.findAny();
        int recCount = defaultCount;

        if (opt2.isPresent()) {
            recCount = Integer.parseInt(opt2.get());
            if (recCount < 1 || recCount > 25) {
                sendError("The recommendation count must be between 1 and 25", e);
                return null;
            }
            words = Arrays.stream(words).filter(s -> !s.matches("\\d+")).toArray(String[]::new);
        }

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
            return new RecommendationsParams(e, null, null, true, recCount);
        } else {
            return new RecommendationsParams(e, datas[0], datas[1], false, recCount);
        }

    }


    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *user* **\n" +
                "\t If user is not specified if will give you a recommendation from a random user " +
                "(biasing more users with more affinity with you), otherwise a rec from that user\n" +
                "\t Alternatively you could also mention two different users\n";

    }
}
