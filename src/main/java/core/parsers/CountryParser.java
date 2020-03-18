package core.parsers;

import com.neovisionaries.i18n.CountryCode;
import core.apis.last.ConcurrentLastFM;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public class CountryParser extends DaoParser {
    private final ConcurrentLastFM lastFM;
    private final MusicBrainzService musicBrainzService;

    public CountryParser(ChuuService dao, ConcurrentLastFM lastFM, MusicBrainzService musicBrainzService) {
        super(dao);
        this.lastFM = lastFM;
        this.musicBrainzService = musicBrainzService;
    }

    @Override
    protected void setUpErrorMessages() {
        errorMessages.put(5, "Couldn't get a country from your now playing artist. You can try the full country name or the 2/3 ISO code");
        errorMessages.put(6, "Could not find any country named like that");

    }

    @Override
    protected String[] parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException {
        User sample;

        if (e.isFromGuild()) {
            List<Member> members = e.getMessage().getMentionedMembers();
            if (!members.isEmpty()) {
                if (members.size() != 1) {
                    sendError("Only one user pls", e);
                    return null;
                }
                sample = members.get(0).getUser();
                words = Arrays.stream(words).filter(s -> !s.equals(sample.getAsMention()) && !s.equals("<@!" + sample.getAsMention().substring(2))).toArray(String[]::new);
            } else {
                sample = e.getMember().getUser();
            }
        } else
            sample = e.getAuthor();
        LastFMData lastFMData = dao.findLastFMData(sample.getIdLong());

        ChartParserAux chartParserAux = new ChartParserAux(words);
        TimeFrameEnum timeFrameEnum = chartParserAux.parseTimeframe(TimeFrameEnum.ALL);
        words = chartParserAux.getMessage();
        String countryCode;
        if (words.length == 0) {
            try {
                NowPlayingArtist nowPlayingInfo = lastFM.getNowPlayingInfo(lastFMData.getName());
                ArtistSummary artistSummary = lastFM.getArtistSummary(nowPlayingInfo.getArtistName(), lastFMData.getName());
                ArtistMusicBrainzDetails artistDetails = musicBrainzService.getArtistDetails(new ArtistInfo(null, artistSummary.getArtistname(), artistSummary.getMbid()));
                countryCode = artistDetails.getCountryCode();
                if (countryCode == null || countryCode.isBlank()) {
                    countryCode = String.join(" ", words);
                }
            } catch (LastFmException ex) {
                sendError(getErrorMessage(5), e);
                return null;
            }
        } else {
            countryCode = String.join(" ", words);
        }
        CountryCode country;
        if (countryCode.length() == 2) {
            if (countryCode.equalsIgnoreCase("uk")) {
                countryCode = "gb";
            }
            country = CountryCode.getByAlpha2Code(countryCode.toUpperCase());
        } else if (countryCode.length() == 3) {
            country = CountryCode.getByAlpha3Code(countryCode.toUpperCase());
        } else {
            String finalCountryCode = countryCode;
            Optional<Locale> opt = Arrays.stream(Locale.getISOCountries()).map(x -> new Locale("en", x)).
                    filter(y -> y.getDisplayCountry().equalsIgnoreCase(finalCountryCode))
                    .findFirst();
            if (opt.isPresent()) {
                country = CountryCode.getByAlpha3Code(opt.get().getISO3Country());
            } else {
                List<CountryCode> byName = CountryCode.findByName(Pattern.compile(".*" + countryCode + ".*"));
                if (byName.isEmpty()) {
                    country = null;
                } else {
                    country = byName.get(0);
                }
            }
        }
        if (country == null) {
            sendError(getErrorMessage(6), e);
            return null;
        }
        if (country == CountryCode.IL) {
            // No political statement at all, just bugfixing
            country = CountryCode.PS;
        }
        return new String[]{lastFMData.getName(), String.valueOf(sample.getIdLong()), country.getAlpha2(), timeFrameEnum.toApiFormat()};
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *COUNTRY*  *timeframe* *username* " +
               "\n\tif username its not specified it defaults to you" +
               "\n\tif timeframe its not specified it defaults to all Time" +
               "\n\tCountry must come in the full name format or in the ISO 3166-1 alpha-2/alpha-3" +
               " format ";
    }
}
