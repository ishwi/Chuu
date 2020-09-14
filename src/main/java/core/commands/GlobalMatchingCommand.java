package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.NumberParser;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.ArtistLbGlobalEntry;
import dao.entities.DiscordUserDisplay;
import dao.entities.LbEntry;
import dao.entities.PrivacyMode;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class GlobalMatchingCommand extends ConcurrentCommand<NumberParameters<ChuuDataParams>> {
    public GlobalMatchingCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.DISCOVERY;
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> getParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can also introduce a number to vary the number of plays needed to award a match, " +
                "defaults to 1";
        return new NumberParser<>(new OnlyUsernameParser(getService()),
                null,
                Integer.MAX_VALUE,
                map, s, false, true);
    }

    @Override
    public String getDescription() {
        return "Like Matching but it will only evaluate users that have a opened up their privacy settings. Do !privacy for more info.";
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {

        NumberParameters<ChuuDataParams> outer = parser.parse(e);
        ChuuDataParams params = outer.getInnerParams();
        if (params == null)
            return;
        int threshold = outer.getExtraParam() == null ? 1 : Math.toIntExact(outer.getExtraParam());
        List<ArtistLbGlobalEntry> list = getService().globalMatchings(params.getLastFMData().getName(), e.isFromGuild() ? e.getGuild().getIdLong() : null, threshold);
        list.forEach(cl -> {
            if (cl.getPrivacyMode() == PrivacyMode.TAG) {
                cl.setDiscordName(e.getJDA().retrieveUserById(cl.getDiscordId()).complete().getAsTag());
            } else if (cl.getPrivacyMode() == PrivacyMode.LAST_NAME) {
                cl.setDiscordName(cl.getLastFmId());
            } else {
                cl.setDiscordName(getUserString(e, cl.getDiscordId()));

            }
        });
        MessageBuilder messageBuilder = new MessageBuilder();

        Long discordId = params.getLastFMData().getDiscordId();
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, discordId);
        String url = userInformation.getUrlImage();
        String usableName = userInformation.getUsername();

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(url);
        StringBuilder a = new StringBuilder();

        if (list.isEmpty()) {
            sendMessageQueue(e, "No one has any matching artist with you :(");
            return;
        }

        for (int i = 0; i < 10 && i < list.size(); i++) {
            a.append(i + 1).append(list.get(i).toString());
        }
        embedBuilder.setDescription(a).setTitle("Global Matching artists with " + usableName)
                .setFooter(String.format("%s has %d total artist!%n", CommandUtil.markdownLessUserString(usableName, discordId, e), getService().getUserArtistCount(params.getLastFMData().getName(), 0)), null);
        e.getChannel().sendMessage(messageBuilder.setEmbed(embedBuilder.build()).build()).queue(mes ->
                new Reactionary<>(list, mes, embedBuilder));
    }
}

