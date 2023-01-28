package core.commands.discovery;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.NumberParser;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import core.util.ServiceView;
import dao.entities.ArtistLbGlobalEntry;
import dao.entities.DiscordUserDisplay;
import dao.entities.UsersWrapper;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class GlobalMatchingCommand extends ConcurrentCommand<NumberParameters<ChuuDataParams>> {


    public GlobalMatchingCommand(ServiceView dao) {
        super(dao, true);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.DISCOVERY;
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can also introduce a number to vary the number of plays needed to award a match, " +
                "defaults to 1";
        return new NumberParser<>(new OnlyUsernameParser(db),
                null,
                Integer.MAX_VALUE,
                map, s, false, true);
    }

    @Override
    public String getDescription() {
        return "Like Matching but it will only evaluate users that have a opened up their privacy settings. Do the command `privacy` for more info.";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globalmatching", "gm", "gmatching");
    }

    @Override
    public String getName() {
        return "Global Matching";
    }

    @Override
    public void onCommand(Context e, @NotNull NumberParameters<ChuuDataParams> params) {


        ChuuDataParams innerParams = params.getInnerParams();
        int threshold = params.getExtraParam() == null ? 1 : Math.toIntExact(params.getExtraParam());
        List<ArtistLbGlobalEntry> list = db.globalMatchings(innerParams.getLastFMData().getName(), e.isFromGuild() ? e.getGuild().getIdLong() : null, threshold);

        long discordId = innerParams.getLastFMData().getDiscordId();


        Set<Long> found;
        if (e.isFromGuild()) {
            found = db.getAll(e.getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toSet());
        } else {
            found = Set.of(e.getAuthor().getIdLong());
        }


        if (list.isEmpty()) {
            sendMessageQueue(e, "No one has any matching artist with you :(");
            return;
        }

        DiscordUserDisplay userInformation = CommandUtil.getUserInfoEscaped(e, discordId);
        String url = userInformation.urlImage();
        String usableName = userInformation.username();

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).setThumbnail(url)
                .setTitle("Global Matching artists with " + usableName)
                .setFooter(String.format("%s has %d total artist!%n", CommandUtil.unescapedUser(usableName, discordId, e), db
                        .getUserArtistCount(innerParams.getLastFMData().
                                getName(), 0)), null);

        AtomicInteger counter = new AtomicInteger(0);
        Function<ArtistLbGlobalEntry, String> mapper = (ArtistLbGlobalEntry s) -> {
            PrivacyUtils.PrivateString privacy =
                    PrivacyUtils.getPublicString(s.getPrivacyMode(), s.getDiscordId(), s.getLastFmId(), counter, e, found);
            return ". **[" +
                    LinkUtils.cleanMarkdownCharacter(privacy.discordName()) +
                    "](" + privacy.lastfmName() +
                    ")** - " + s.getEntryCount() +
                    " artists\n";
        };
        new PaginatorBuilder<>(e, embedBuilder, list).memoized(mapper).build().queue();
    }
}

