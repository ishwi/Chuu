package core.commands.discovery;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.otherlisteners.Reactionary;
import core.parsers.Parser;
import core.parsers.RandomAlbumParser;
import core.parsers.params.RandomUrlParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static core.commands.rym.AlbumRatings.getStartsFromScore;

public class RandomLinkDetailsCommand extends ConcurrentCommand<RandomUrlParameters> {
    public RandomLinkDetailsCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.DISCOVERY;
    }

    @Override
    public Parser<RandomUrlParameters> initParser() {
        return new RandomAlbumParser(db);
    }

    @Override
    public String getDescription() {
        return "Details of a random url";
    }

    @Override
    public List<String> getAliases() {
        return List.of("randomdetails", "rdetails");
    }

    @Override
    public String getName() {
        return "Random details";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull RandomUrlParameters params) {


        String url = params.getUrl();
        if (StringUtils.isEmpty(url)) {
            RandomUrlEntity randomUrl;
            if (params.hasOptional("server") && e.isFromGuild()) {
                randomUrl = db.getRandomUrlFromServer(e.getGuild().getIdLong());
            } else {
                randomUrl = db.getRandomUrl();
            }
            if (randomUrl == null) {
                sendMessageQueue(e, "Try to give an url to this command.");
                return;
            }
            url = db.getRandomUrl().url();
        }
        RandomUrlDetails randomUrl = db.findRandomUrlDetails(url);
        if (randomUrl == null) {
            char messagePrefix = CommandUtil.getMessagePrefix(e);
            sendMessageQueue(e, "The given url was not in the pool therefore it cannot be rated. You might also consider adding it using the " + messagePrefix + "random .");
            return;
        }

        LastFMData own = null;
        try {
            own = db.findLastFMData(randomUrl.discordId());
        } catch (InstanceNotFoundException exception) {
            exception.printStackTrace();
        }
        AtomicInteger atomicInteger = new AtomicInteger(1);
        Set<Long> ids;
        if (e.isFromGuild()) {
            ids = db.getAll(e.getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toSet());
        } else {
            ids = Set.of(e.getAuthor().getIdLong());
        }
        Function<Byte, String> starFormatter = getStartsFromScore();

        Function<RandomRating, String> mapper = (r) -> {
            var publicString = PrivacyUtils.getPublicString(r.privacyMode(), r.discordId(), r.lastfmId(), atomicInteger, e, ids);
            return ". **[" + publicString.discordName() +
                    "](" + publicString.lastfmName() +
                    ")** - " + starFormatter.apply(r.rating()) +
                    "\n";
        };

        List<Memoized<RandomRating, String>> z = randomUrl.ratings().stream().map(x -> new Memoized<>(x, mapper)).toList();


        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 5 && i < z.size(); i++) {
            a.append(i + 1).append(z.get(i).toString());
        }
        NumberFormat formatter = new DecimalFormat("#0.##");
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(randomUrl.url() + " details", randomUrl.url())
                .setColor(ColorService.computeColor(e))
                .setDescription(a)
                .setFooter("Has been rated by %d %s | average: %s%nSubmited by %s".formatted(randomUrl.count(), CommandUtil.singlePlural(randomUrl.count(), "person", "people"), formatter.format(randomUrl.avg()),
                        own == null ? "" : PrivacyUtils.getPublicString(own.getPrivacyMode(), own.getDiscordId(), own.getName(), new AtomicInteger(0), e, ids).discordName()
                ));
        e.getChannel().sendMessage(embedBuilder.build()).queue(message1 ->
                new Reactionary<>(z, message1, 5, embedBuilder));

    }


}
