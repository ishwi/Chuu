package core.commands.stats;

import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.TwoUsersTimeframeParser;
import core.parsers.params.ChartParameters;
import core.parsers.params.TwoUsersTimeframeParamaters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class TasteCommand extends BaseTasteCommand<TwoUsersTimeframeParamaters> {
    private final Map<String, Pair<Integer, Integer>> timeFrameMap = new ConcurrentHashMap<>();

    public TasteCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public String getEntity(TwoUsersTimeframeParamaters params) {
        return "artists";
    }

    @Override
    public String hasCustomUrl(TwoUsersTimeframeParamaters params) {
        return null;
    }

    @Override
    public Parser<TwoUsersTimeframeParamaters> initParser() {
        TwoUsersTimeframeParser twoUsersParser = new TwoUsersTimeframeParser(getService(), new OptionalEntity("list", "display in a list format"));
        twoUsersParser.setExpensiveSearch(true);
        return twoUsersParser;
    }

    @Override
    public String getDescription() {
        return "Compare Your musical taste with another user";
    }

    @Override
    public List<String> getAliases() {
        return List.of("taste", "t", "compare");
    }


    @Override
    public Pair<LastFMData, LastFMData> getUserDatas(MessageReceivedEvent e, TwoUsersTimeframeParamaters params) {
        return Pair.of(params.getFirstUser(), params.getSecondUser());
    }

    @Override
    public ResultWrapper<UserArtistComparison> getResult(LastFMData og, LastFMData second, TwoUsersTimeframeParamaters params) {
        boolean isList = params.hasOptional("list");
        if (params.getTimeFrameEnum() == TimeFrameEnum.ALL) {
            return getService().getSimilarities(List.of(og.getName(), second.getName()), isList ? 200 : 10);
        } else {
            Queue<UrlCapsule> lQ = new ArrayDeque<>();
            Queue<UrlCapsule> rQ = new ArrayDeque<>();
            try {
                int ogSize = lastFM.getChart(og, CustomTimeFrame.ofTimeFrameEnum(params.getTimeFrameEnum()),
                        1000, 1, TopEntity.ARTIST, ChartUtil.getParser(CustomTimeFrame.ofTimeFrameEnum(params.getTimeFrameEnum()), TopEntity.ARTIST, ChartParameters.toListParams(), lastFM, og), lQ);
                int secondSize = lastFM.getChart(second, CustomTimeFrame.ofTimeFrameEnum(params.getTimeFrameEnum()),
                        1000, 1, TopEntity.ARTIST, ChartUtil.getParser(CustomTimeFrame.ofTimeFrameEnum(params.getTimeFrameEnum()), TopEntity.ARTIST, ChartParameters.toListParams(), lastFM, og), rQ);
                HashSet<UrlCapsule> set = new HashSet<>(lQ);
                Map<UrlCapsule, UrlCapsule> map = rQ.stream().collect(Collectors.toMap(x -> x, x -> x));
                record Holder(UrlCapsule first, UrlCapsule second, double total) {

                }
                ResultWrapper<UserArtistComparison> collect = set.stream().map(o -> {
                    UrlCapsule urlCapsule = map.get(o);
                    if (urlCapsule != null) {
                        record Two(UrlCapsule first, UrlCapsule second) {
                        }
                        return new Two(o, urlCapsule);
                    }
                    return null;
                }).filter(Objects::nonNull).map(x -> {
                    UrlCapsule a = x.first;
                    UrlCapsule b = x.second;
                    double score = ((a.getPlays() + b.getPlays()) / (

                            abs(a.getPlays() - b.getPlays()) + 1d)) *
                            (((a.getPlays() + b.getPlays())) * 2.5) *

                            ((a.getPlays() > (10 * b.getPlays()) || b.getPlays() > (10 * a.getPlays()) && (Math.min(a.getPlays(), b.getPlays()) < 400)) ? 0.01 : 2);


                    return new Holder(a, b, score);
                }).sorted((a, b) -> (int) (b.total - a.total))
                        .map(x -> {
                            UrlCapsule left = x.first;
                            UrlCapsule right = x.second;
                            return new UserArtistComparison(left.getPlays(), right.getPlays(), left.getArtistName(), og.getName(), second.getName(), null);
                        })
                        .collect(Collectors.collectingAndThen(Collectors.toList(), t -> new ResultWrapper<>(t.size(), t)));
                if (collect.getRows() != 0 && CommandUtil.getEffectiveMode(og.getRemainingImagesMode(), params) == RemainingImagesMode.IMAGE) {
                    this.timeFrameMap.put(params.getE().getMessageId(), Pair.of(ogSize, secondSize));
                }
                return collect;

            } catch (LastFmException e) {

                return new ResultWrapper<>(0, new ArrayList<>());
            }

        }

    }

    @Override
    public Pair<Integer, Integer> getTasteBar(ResultWrapper<UserArtistComparison> resultWrapper, UserInfo og, UserInfo second, TwoUsersTimeframeParamaters params) {
        Pair<Integer, Integer> remove = this.timeFrameMap.remove(params.getE().getMessageId());
        return remove != null ? remove : Pair.of(og.getPlayCount(), second.getPlayCount());
    }


    @Override
    public String getName() {
        return "Taste";
    }

}
