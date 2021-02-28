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
import dao.entities.Affinity;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class GlobalAffinityCommand extends ConcurrentCommand<NumberParameters<ChuuDataParams>> {

    public GlobalAffinityCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
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
                "defaults to 30";
        return new NumberParser<>(new OnlyUsernameParser(db),
                30L,
                Integer.MAX_VALUE,
                map, s, false, true);
    }

    @Override
    public String getDescription() {
        return "Gets your affinity with the rest of the bot users that have opened up their privacy settings";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globalaffinity", "gaff", "globalsoulmate");
    }

    @Override
    public String getName() {
        return "Global Affinity";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull NumberParameters<ChuuDataParams> params) throws InstanceNotFoundException {


        LastFMData ogData = db.findLastFMData(e.getAuthor().getIdLong());
        int threshold = Math.toIntExact(params.getExtraParam());
        List<dao.entities.GlobalAffinity> serverAffinity = db.getGlobalAffinity(ogData.getName(), threshold);
        List<dao.entities.GlobalAffinity> collect = serverAffinity.stream().sorted(Comparator.comparing(Affinity::getAffinity).reversed()).collect(Collectors.toList());

        StringBuilder stringBuilder = new StringBuilder();
        List<String> string = collect.stream().map(x -> {
            String name = PrivacyUtils.getPublicStr(x.getPrivacyMode(), x.getDiscordId(), x.getReceivingLastFmId(), e);
                    return String.format(". [%s](%s) - %.2f%%%s matching%n", name,
                            CommandUtil.getLastFmUser(x.getReceivingLastFmId()),
                            (x.getAffinity() > 1 ? 1 : x.getAffinity()) * 100, x.getAffinity() > 1 ? "+" : "");
                }
        ).collect(Collectors.toList());
        for (
                int i = 0, size = collect.size();
                i < 10 && i < size; i++) {
            String text = string.get(i);
            stringBuilder.append(i + 1).append(text);
        }

        DiscordUserDisplay uinfo = CommandUtil.getUserInfoConsideringGuildOrNot(e, e.getAuthor().getIdLong());
        String name = e.getJDA().getSelfUser().getName();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setDescription(stringBuilder)
                .setTitle(uinfo.getUsername() + "'s soulmates in " + CommandUtil.cleanMarkdownCharacter(name))
                .setColor(ColorService.computeColor(e))
                .setFooter(String.format("%s's global affinity using a threshold of %d plays!%n", CommandUtil.markdownLessString(uinfo.getUsername()), threshold), null)
                .setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
        e.getChannel().sendMessage(embedBuilder.build())
                .queue(message1 -> new Reactionary<>(string, message1, embedBuilder));
    }

}
