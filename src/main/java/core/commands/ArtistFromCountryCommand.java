package core.commands;

import com.neovisionaries.i18n.CountryCode;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.CountryParser;
import dao.ChuuService;
import dao.entities.ArtistUserPlays;
import dao.entities.TimeFrameEnum;
import dao.entities.UrlCapsule;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ArtistFromCountryCommand extends ConcurrentCommand {


    private final MusicBrainzService mb;

    public ArtistFromCountryCommand(ChuuService dao) {
        super(dao);
        this.parser = new CountryParser(dao);
        mb = MusicBrainzServiceSingleton.getInstance();
    }

    @Override
    public String getDescription() {
        return "Your top artist that are from a specific country";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("from");
    }

    @Override
    public String getName() {
        return "Artist from a country";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] message = parser.parse(e);
        if (message == null) {
            return;
        }

        long discordId = Long.parseLong(message[1]);

        String countryCode = message[2];
        String timeframe = message[3];

        CountryCode country = CountryCode.getByAlpha2Code(countryCode);
        BlockingQueue<UrlCapsule> queue = new ArrayBlockingQueue<>(1000);
        lastFM.getUserList(message[0], timeframe, 1000, 1, false, queue);

        List<ArtistUserPlays> list = this.mb.getArtistFromCountry(country, queue, discordId);
        StringBuilder userName = new StringBuilder();
        StringBuilder userUrl = new StringBuilder();
        CommandUtil.getUserInfoConsideringGuildOrNot(userName, userUrl, e, discordId);
        String unchangedUsername = userName.toString();

        String usableTime = timeframe
                .equals("overall")
                ? ""
                : ("in the last " + TimeFrameEnum.fromCompletePeriod(timeframe).toString().toLowerCase());
        if (list.isEmpty()) {
            sendMessageQueue(e, unchangedUsername + " doesnt have any artist from " + ":flag_" + country.getAlpha2().toLowerCase() + ": " + usableTime);
            return;
        }
        StringBuilder a = new StringBuilder();

        for (int i = 0; i < 10 && i < list.size(); i++) {
            a.append(i + 1).append(list.get(i).toString());
        }

        userName.append("'s top artists from  :flag_").append(country.getAlpha2().toLowerCase()).append(":");
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(userUrl.toString())
                .setFooter(unchangedUsername + " has " + list.size() +
                           (list.size() == 1 ? " artist " : " artists ") + "from " + country.getName() + " " + usableTime, null)
                .setTitle(userName.toString())
                .setDescription(a);
        messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue(mes ->
                executor.execute(() -> new Reactionary<>(list, mes, embedBuilder)));
    }
}
