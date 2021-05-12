package core.commands.discovery;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.stats.AffinityCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.NoOpParser;
import core.parsers.NumberParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.parsers.params.NumberParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.Affinity;
import dao.entities.LastFMData;
import dao.entities.PrivacyMode;
import dao.entities.ScrobbledArtist;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class GlobalRecommendationCommand extends ConcurrentCommand<NumberParameters<CommandParameters>> {
    public GlobalRecommendationCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.DISCOVERY;
    }

    @Override
    public Parser<NumberParameters<CommandParameters>> initParser() {
        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and smaller than 25");
        String s = "You can also introduce a number to vary the number of recommendations that you will receive, " +
                   "defaults to 1";
        NumberParser<CommandParameters, NoOpParser> parser = new NumberParser<>(NoOpParser.INSTANCE,
                1L,
                Integer.MAX_VALUE,
                map, s, false, true);
        parser.addOptional(new OptionalEntity("repeated", "gives you a repeated recommendation"));
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
    protected void onCommand(Context e, @NotNull NumberParameters<CommandParameters> params) throws InstanceNotFoundException {


        long firstDiscordID;
        long secondDiscordID;
        LastFMData lastFMData = db.findLastFMData(e.getAuthor().getIdLong());
        List<dao.entities.GlobalAffinity> serverAffinity = db.getGlobalAffinity(lastFMData.getName(), AffinityCommand.DEFAULT_THRESHOLD);
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

        List<ScrobbledArtist> recs = db.getRecommendation(secondDiscordID, firstDiscordID, params.hasOptional("repeated"), Math.toIntExact(params.getExtraParam()));

        String receiver = "you";
        if (firstDiscordID != e.getAuthor().getIdLong()) {
            receiver = getUserString(e, firstDiscordID);
        }

        String giver;
        try {
            LastFMData cl = db.findLastFMData(secondDiscordID);
            if (cl.getPrivacyMode() == PrivacyMode.TAG) {
                giver = e.getJDA().retrieveUserById(cl.getDiscordId(), false).complete().getAsTag();
            } else if (cl.getPrivacyMode() == PrivacyMode.LAST_NAME) {
                giver = cl.getName();
            } else {
                giver = getUserString(e, cl.getDiscordId());

            }
        } catch (InstanceNotFoundException ex) {
            giver = "Unknown";
        }
        if (recs.isEmpty()) {
            sendMessageQueue(e, String.format("Couldn't get %s any recommendation from %s", receiver, giver));
        } else {
            if (recs.size() == 1) {
                sendMessageQueue(e, String.format("**%s** has recommended %s to listen to **%s** (they have %d plays)",
                        giver, receiver, CommandUtil.cleanMarkdownCharacter(recs.get(0).getArtist()), recs.get(0).getCount()));
                db.insertRecommendation(secondDiscordID, firstDiscordID, recs.get(0).getArtistId());
            } else {
                StringBuilder s = new StringBuilder();
                for (ScrobbledArtist rec : recs) {
                    s.append((String.format("# [%s](%s): %d plays%n", CommandUtil.cleanMarkdownCharacter(rec.getArtist()), LinkUtils.getLastFmArtistUrl(rec.getArtist()), rec.getCount())));
                }
                EmbedBuilder embedBuilder = new ChuuEmbedBuilder();

                embedBuilder.setTitle(String.format("%s recommendations for %s", giver, receiver))
                        .setColor(ColorService.computeColor(e))
                        .setDescription(s);
                e.sendMessage(embedBuilder.build()).queue();
            }
        }
    }
}
