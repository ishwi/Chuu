package core.commands.random;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.Parser;
import core.parsers.RandomAlbumParser;
import core.parsers.params.RandomUrlParameters;
import dao.ServiceView;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static core.commands.rym.AlbumRatings.getStartsFromScore;

public class RandomLinkDetailsCommand extends ConcurrentCommand<RandomUrlParameters> {
    public RandomLinkDetailsCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.RANDOM;
    }

    @Override
    public Parser<RandomUrlParameters> initParser() {
        return new RandomAlbumParser(db);
    }

    @Override
    public String slashName() {
        return "details";
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
    public void onCommand(Context e, @Nonnull RandomUrlParameters params) {


        String url = params.getUrl();
        if (StringUtils.isEmpty(url)) {
            RandomUrlEntity randomUrl;
            if (params.hasOptional("server") && e.isFromGuild()) {
                randomUrl = db.getRandomUrlFromServer(e.getGuild().getIdLong(), null);
            } else {
                randomUrl = db.getRandomUrl(null);
            }
            if (randomUrl == null) {
                sendMessageQueue(e, "Try to give an url to this command.");
                return;
            }
            url = db.getRandomUrl(null).url();
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
            Chuu.getLogger().info(exception.getMessage(), exception);
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


        NumberFormat formatter = new DecimalFormat("#0.##");
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setTitle(randomUrl.url() + " details", randomUrl.url())
                .setFooter("Has been rated by %d %s | average: %s%nSubmited by %s".formatted(randomUrl.count(), CommandUtil.singlePlural(randomUrl.count(), "person", "people"), formatter.format(randomUrl.avg()),
                        own == null ? "" : PrivacyUtils.getPublicString(own.getPrivacyMode(), own.getDiscordId(), own.getName(), new AtomicInteger(0), e, ids).discordName()
                ));

        new PaginatorBuilder<>(e, embedBuilder, randomUrl.ratings()).memoized(mapper).pageSize(5).build().queue();

    }


}
