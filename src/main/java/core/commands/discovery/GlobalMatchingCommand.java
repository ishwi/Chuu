package core.commands.discovery;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.otherlisteners.Reactionary;
import core.parsers.NumberParser;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.ArtistLbGlobalEntry;
import dao.entities.DiscordUserDisplay;
import dao.entities.Memoized;
import dao.entities.UsersWrapper;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class GlobalMatchingCommand extends ConcurrentCommand<NumberParameters<ChuuDataParams>> {


    public GlobalMatchingCommand(ChuuService dao) {
        super(dao);
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
    protected void onCommand(MessageReceivedEvent e, @NotNull NumberParameters<ChuuDataParams> params) {


        ChuuDataParams innerParams = params.getInnerParams();
        int threshold = params.getExtraParam() == null ? 1 : Math.toIntExact(params.getExtraParam());
        List<ArtistLbGlobalEntry> list = db.globalMatchings(innerParams.getLastFMData().getName(), e.isFromGuild() ? e.getGuild().getIdLong() : null, threshold);

        Long discordId = innerParams.getLastFMData().getDiscordId();
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, discordId);
        String url = userInformation.getUrlImage();
        String usableName = userInformation.getUsername();

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(ColorService.computeColor(e))
                .setThumbnail(url);
        Set<Long> found;
        if (e.isFromGuild()) {
            found = db.getAll(e.getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toSet());
        } else {
            found = Set.of(e.getAuthor().getIdLong());
        }

        StringBuilder a = new StringBuilder();
        AtomicInteger c = new AtomicInteger(0);
        Function<ArtistLbGlobalEntry, String> mapper = (ArtistLbGlobalEntry s) -> {
            PrivacyUtils.PrivateString privacy =
                    PrivacyUtils.getPublicString(s.getPrivacyMode(), s.getDiscordId(), s.getLastFmId(), c, e, found);
            return ". **[" +
                    LinkUtils.cleanMarkdownCharacter(privacy.discordName()) +
                    "](" + privacy.lastfmName() +
                    ")** - " + s.getEntryCount() +
                    " artists\n";
        };

        List<Memoized<ArtistLbGlobalEntry, String>> strings = list.stream().map(x -> new Memoized<>(x, mapper)).toList();
        if (list.isEmpty()) {
            sendMessageQueue(e, "No one has any matching artist with you :(");
            return;
        }
        for (int i = 0; i < 10 && i < list.size(); i++) {
            a.append(i + 1).append((strings.get(i).toString()));
        }
        embedBuilder.setDescription(a).setTitle("Global Matching artists with " + usableName)
                .setFooter(String.format("%s has %d total artist!%n", CommandUtil.markdownLessUserString(usableName, discordId, e), db
                        .getUserArtistCount(innerParams.getLastFMData().
                                getName(), 0)), null);
        e.getChannel().sendMessage(embedBuilder.build()).queue(mes ->
                new Reactionary<>(strings, mes, embedBuilder));
    }
}

