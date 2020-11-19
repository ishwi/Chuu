package core.commands;

import core.Chuu;
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
import dao.entities.PrivacyMode;
import dao.entities.UsersWrapper;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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
        return new NumberParser<>(new OnlyUsernameParser(getService()),
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
    void onCommand(MessageReceivedEvent e, @NotNull NumberParameters<ChuuDataParams> params) throws LastFmException, InstanceNotFoundException {


        ChuuDataParams innerParams = params.getInnerParams();
        int threshold = params.getExtraParam() == null ? 1 : Math.toIntExact(params.getExtraParam());
        List<ArtistLbGlobalEntry> list = getService().globalMatchings(innerParams.getLastFMData().getName(), e.isFromGuild() ? e.getGuild().getIdLong() : null, threshold);

        Long discordId = innerParams.getLastFMData().getDiscordId();
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, discordId);
        String url = userInformation.getUrlImage();
        String usableName = userInformation.getUsername();

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(url);
        Set<Long> found;
        if (e.isFromGuild()) {
            found = getService().getAll(e.getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toSet());
        } else {
            found = Set.of(e.getAuthor().getIdLong());
        }

        StringBuilder a = new StringBuilder();
        AtomicInteger c = new AtomicInteger(0);
        List<Object> strings = list.stream().map(x -> new Object() {
            private String calculatedString = null;

            @Override
            public String toString() {
                if (calculatedString == null) {

                    PrivacyMode privacyMode = x.getPrivacyMode();
                    if (found.contains(x.getDiscordId())) {
                        privacyMode = PrivacyMode.DISCORD_NAME;
                    }


                    switch (privacyMode) {

                        case STRICT:
                        case NORMAL:
                            x.setDiscordName(" **Private User #" + c.getAndIncrement() + "**");
                            break;
                        case DISCORD_NAME:
                            x.setDiscordName(CommandUtil.getUserInfoNotStripped(e, x.getDiscordId()).getUsername() + "**");
                            break;
                        case TAG:
                            x.setDiscordName(" **" + e.getJDA().retrieveUserById(x.getDiscordId()).complete().getAsTag() + "**");
                            break;
                        case LAST_NAME:
                            x.setDiscordName(" **" + x.getLastFmId() + " (last.fm)**");
                            break;
                    }
                    calculatedString = ". [" +
                            LinkUtils.cleanMarkdownCharacter(x.getDiscordName()) +
                            "](" + Chuu.getLastFmId(x.getLastFmId()) +
                            ") - " + x.getEntryCount() +
                            " artists\n";
                }
                return calculatedString;
            }
        }).collect(Collectors.toList());
        if (list.isEmpty()) {
            sendMessageQueue(e, "No one has any matching artist with you :(");
            return;
        }
        for (int i = 0; i < 10 && i < list.size(); i++) {
            a.append(i + 1).append((strings.get(i).toString()));
        }
        embedBuilder.setDescription(a).setTitle("Global Matching artists with " + usableName)
                .setFooter(String.format("%s has %d total artist!%n", CommandUtil.markdownLessUserString(usableName, discordId, e), getService()
                        .getUserArtistCount(innerParams.getLastFMData().
                                getName(), 0)), null);
        e.getChannel().sendMessage(embedBuilder.build()).queue(mes ->
                new Reactionary<>(list, mes, embedBuilder));
    }
}

