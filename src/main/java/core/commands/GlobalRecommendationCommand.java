package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.NumberParser;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import dao.ChuuService;
import dao.entities.Affinity;
import dao.entities.LastFMData;
import dao.entities.PrivacyMode;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class GlobalRecommendationCommand extends ConcurrentCommand<NumberParameters<ChuuDataParams>> {
    public GlobalRecommendationCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.DISCOVERY;
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> getParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and smaller than 25");
        String s = "You can also introduce a number to vary the number of recommendations that you will receive, " +
                "defaults to 1";
        NumberParser<ChuuDataParams, OnlyUsernameParser> parser = new NumberParser<>(new OnlyUsernameParser(getService()),
                1L,
                Integer.MAX_VALUE,
                map, s, false, true);
        parser.addOptional(new OptionalEntity("--repeated", "gives you a repeated recommendation"));
        return parser;
    }

    @Override
    public String getDescription() {
        return "Gets you a recommendation from bot users that have opened up their privacy settings";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globalrecommendation", "grec");
    }

    @Override
    public String getName() {
        return "Global Recommendation";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        NumberParameters<ChuuDataParams> outerParms = parser.parse(e);
        ChuuDataParams params = outerParms.getInnerParams();
        if (params == null) {
            return;
        }
        long firstDiscordID;
        long secondDiscordID;
        LastFMData lastFMData = getService().findLastFMData(e.getAuthor().getIdLong());
        List<dao.entities.GlobalAffinity> serverAffinity = getService().getGlobalAffinity(lastFMData.getName(), e.getGuild().getIdLong(), AffinityCommand.DEFAULT_THRESHOLD);
        if (serverAffinity.isEmpty()) {
            sendMessageQueue(e, "Couldn't get you any global recommendation :(");
            return;

        }
        TreeMap<Float, Affinity> integerAffinityTreeMap = new TreeMap<>();
        float counter = 1;
        for (Affinity affinity : serverAffinity) {
            integerAffinityTreeMap.put(counter, affinity);
            counter += affinity.getAffinity() + 0.001f;
        }
        int numberOfTries = 2;
        Map.Entry<Float, Affinity> floatAffinityEntry = null;
        while (numberOfTries-- != 0 && floatAffinityEntry == null) {
            double v = CommandUtil.rand.nextDouble();
            floatAffinityEntry = integerAffinityTreeMap.floorEntry((float) (v * counter));
        }

        if (floatAffinityEntry == null) {
            sendMessageQueue(e, "Couldn't get you any global recommendation :(");
            return;
        }
        Affinity affinity = floatAffinityEntry.getValue();

        firstDiscordID = e.getAuthor().getIdLong();
        secondDiscordID = affinity.getDiscordId();

        List<ScrobbledArtist> recs = getService().getRecommendation(secondDiscordID, firstDiscordID, params.hasOptional("--repeated"), Math.toIntExact(outerParms.getExtraParam()));

        String receiver = "you";
        if (firstDiscordID != e.getAuthor().getIdLong()) {
            receiver = getUserString(e, firstDiscordID);
        }

        String giver;
        try {
            LastFMData cl = getService().findLastFMData(secondDiscordID);
            if (cl.getPrivacyMode() == PrivacyMode.TAG) {
                giver = e.getJDA().retrieveUserById(cl.getDiscordId()).complete().getAsTag();
            } else if (cl.getPrivacyMode() == PrivacyMode.LAST_NAME) {
                giver = cl.getName();
            } else {
                giver = getUserString(e, cl.getDiscordId());

            }
        } catch (InstanceNotFoundException ex) {
            giver = "Unkown";
        }
        if (recs.isEmpty()) {
            sendMessageQueue(e, String.format("Couldn't get %s any recommendation from %s", receiver, giver));
        } else {
            if (recs.size() == 1) {
                sendMessageQueue(e, String.format("**%s** has recommended %s to listen to **%s** (they have %d plays)",
                        giver, receiver, CommandUtil.cleanMarkdownCharacter(recs.get(0).getArtist()), recs.get(0).getCount()));
                getService().insertRecommendation(secondDiscordID, firstDiscordID, recs.get(0).getArtistId());
            } else {
                StringBuilder s = new StringBuilder();
                for (ScrobbledArtist rec : recs) {
                    s.append((String.format("# [%s](%s): %d plays%n", CommandUtil.cleanMarkdownCharacter(rec.getArtist()), CommandUtil.getLastFmArtistUrl(rec.getArtist()), rec.getCount())));
                }
                EmbedBuilder embedBuilder = new EmbedBuilder();

                embedBuilder.setTitle(String.format("%s recommendations for %s", giver, receiver))
                        .setColor(CommandUtil.randomColor())
                        .setDescription(s);
                e.getChannel().sendMessage(new MessageBuilder().setEmbed(embedBuilder.build()).build()).queue();
            }
        }
    }
}
