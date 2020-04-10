package core.parsers;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;

public class ArtistTimeFrameParser extends ArtistParser {
    private final static TimeFrameEnum defaultTFE = TimeFrameEnum.ALL;

    public ArtistTimeFrameParser(ChuuService dao, ConcurrentLastFM lastFM) {
        super(dao, lastFM);
    }

    @Override
    public String[] parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException {
        User sample;
        TimeFrameEnum timeFrame = defaultTFE;

        ChartParserAux chartParserAux = new ChartParserAux(words, false);
        timeFrame = chartParserAux.parseTimeframe(timeFrame);
        words = chartParserAux.getMessage();
        String[] strings;
        if (e.isFromGuild()) {
            List<Member> members = e.getMessage().getMentionedMembers();
            if (!members.isEmpty()) {
                if (members.size() != 1) {
                    sendError("Only one user pls", e);
                    return null;
                }
                sample = members.get(0).getUser();
                words = Arrays.stream(words).filter(s -> !s.equals(sample.getAsMention()) && !s
                        .equals("<@!" + sample.getAsMention().substring(2))).toArray(String[]::new);
            } else {
                sample = e.getMember().getUser();
            }
        } else
            sample = e.getAuthor();

        if (words.length == 0) {

            NowPlayingArtist np;

            String userName = dao.findLastFMData(sample.getIdLong()).getName();
            np = lastFM.getNowPlayingInfo(userName);

            strings = doSomethingWithNp(np, sample, e);

        } else {

            strings = doSomethingWithString(words, sample, e);
        }
        return ArrayUtils.add(strings, timeFrame.toApiFormat());

    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *artist* *[w,m,q,s,y,a]*** \n" +
               "\tIf time is not specified defaults to " + defaultTFE.toString() + "\n" +
               "\tDue to being able to provide an artist name and the timeframe, some" +
               " conflicts may occure if the timeframe keyword appears on the artist name, to reduce possible" +
               " conflicts only the one letter shorthand is available for the timeframe";
    }
}
