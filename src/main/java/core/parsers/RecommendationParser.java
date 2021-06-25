package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.exceptions.LastFmException;
import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.RecommendationsParams;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RecommendationParser extends DaoParser<RecommendationsParams> {
    private static final OptionData recs;

    static {
        recs = new OptionData(OptionType.INTEGER, "number-of-recs", "Indicates the number of recommendations allowed. Defaults to 1");
        IntStream.range(1, 26).forEachOrdered(t -> recs.addChoice(String.valueOf(t), t));
    }

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
    public RecommendationsParams parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        OptionMapping recCount = e.getOption(recs.getName());

        int threshold = defaultCount;
        if (recCount != null)
            threshold = Math.toIntExact(recCount.getAsLong());
        User user = InteractionAux.parseUser(e);
        boolean doServer = user == e.getUser();
        if (!doServer) {
            LastFMData first = findLastfmFromID(e.getUser(), ctx);
            LastFMData second = findLastfmFromID(user, ctx);
            return new RecommendationsParams(ctx, first, second, false, threshold);
        } else {
            return new RecommendationsParams(ctx, null, null, true, threshold);
        }
    }

    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
    }

    @Override
    public RecommendationsParams parseLogic(Context e, String[] words) throws InstanceNotFoundException {

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
        } else if (datas[0].getDiscordId() == datas[1].getDiscordId()) {
            e.sendMessage("Don't use the same person twice\n").queue();
            return null;
        }
        if (noUserFlag) {
            return new RecommendationsParams(e, null, null, true, recCount);
        } else {
            return new RecommendationsParams(e, datas[0], datas[1], false, recCount);
        }

    }

    @Override
    public List<Explanation> getUsages() {

        return List.of(
                new PermissiveUserExplanation() {
                    @Override
                    public ExplanationLine explanation() {
                        return new ExplanationLine(super.explanation().header(),
                                """
                                        If an user is not specified it will give you a recommendation from a random user (biasing more users with more affinity with you), otherwise a rec from that user
                                        Alternatively you could also mention two different users.""", super.explanation().options());
                    }
                }
                ,
                () -> new ExplanationLine(recs.getName(), recs.getDescription(), recs));
    }


}
