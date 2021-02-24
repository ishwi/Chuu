package core.commands.stats;

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
import dao.entities.DiscordUserDisplay;
import dao.entities.LbEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class MatchingArtistCommand extends ConcurrentCommand<NumberParameters<ChuuDataParams>> {


    public MatchingArtistCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
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
        return "Users ordered by matching number of artists";
    }

    @Override
    public List<String> getAliases() {
        return List.of("matching");
    }

    @Override
    public String getName() {
        return "Matching artists";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull NumberParameters<ChuuDataParams> params) {

        ChuuDataParams innerParams = params.getInnerParams();

        long discordId = innerParams.getLastFMData().getDiscordId();
        int threshold = params.getExtraParam() == null ? 1 : Math.toIntExact(params.getExtraParam());
        List<LbEntry> list = db.matchingArtistsCount(innerParams.getLastFMData().getName(), e.getGuild().getIdLong(), threshold);
        list.forEach(cl -> cl.setDiscordName(getUserString(e, cl.getDiscordId(), cl.getLastFmId())));
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, discordId);
        String url = userInformation.getUrlImage();
        String usableName = userInformation.getUsername();

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(ColorService.computeColor(e))
                .setThumbnail(url);
        StringBuilder a = new StringBuilder();

        if (list.isEmpty()) {
            sendMessageQueue(e, "No one has any matching artist with you :(");
            return;
        }

        List<String> strings = list.stream().map(PrivacyUtils::toString).collect(Collectors.toUnmodifiableList());
        for (int i = 0; i < 10 && i < strings.size(); i++) {
            a.append(i + 1).append(strings.get(i));
        }
        int count = db.getUserArtistCount(innerParams.getLastFMData().getName(), 0);
        embedBuilder.setDescription(a).setTitle("Matching artists with " + usableName)
                .setFooter(String.format("%s has %d total %s!%n", CommandUtil.markdownLessUserString(usableName, discordId, e), count, CommandUtil.singlePlural(count, "artist", "artists")), null);
        e.getChannel().sendMessage(embedBuilder.build()).queue(mes ->
                new Reactionary<>(strings, mes, embedBuilder));
    }
}
