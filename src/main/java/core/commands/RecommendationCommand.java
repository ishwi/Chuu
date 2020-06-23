package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.RecommendationParser;
import core.parsers.params.RecommendationsParams;
import dao.ChuuService;
import dao.entities.Affinity;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RecommendationCommand extends ConcurrentCommand<RecommendationsParams> {
    public RecommendationCommand(ChuuService dao) {
        super(dao);
        this.respondInPrivate = false;
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.DISCOVERY;
    }

    @Override
    public Parser<RecommendationsParams> getParser() {
        return new RecommendationParser(getService());
    }

    @Override
    public String getDescription() {
        return "Gets you an artist that you have never scrobbled!";
    }

    @Override
    public List<String> getAliases() {
        return List.of("recommendation", "rec");
    }

    @Override
    public String getName() {
        return "Random recommendation from other user";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        RecommendationsParams rp = parser.parse(e);
        if (rp == null) {
            return;
        }
        long firstDiscordID;
        long secondDiscordID;
        if (rp.isNoUser()) {
            LastFMData lastFMData = getService().findLastFMData(e.getAuthor().getIdLong());
            List<Affinity> serverAffinity = getService().getServerAffinity(lastFMData.getName(), e.getGuild().getIdLong(), AffinityCommand.DEFAULT_THRESHOLD);
            if (serverAffinity.isEmpty()) {
                sendMessageQueue(e, "Couldn't get you any recommendation :(");
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
                sendMessageQueue(e, "Couldn't get you any recommendation :(");
                return;
            }
            Affinity affinity = floatAffinityEntry.getValue();

            firstDiscordID = e.getAuthor().getIdLong();
            secondDiscordID = affinity.getDiscordId();
        } else {
            firstDiscordID = rp.getFirstUser().getDiscordId();
            secondDiscordID = rp.getSecondUser().getDiscordId();
        }
        List<ScrobbledArtist> recs = getService().getRecommendation(secondDiscordID, firstDiscordID, rp.isShowRepeated(), Math.toIntExact(rp.getRecCount()));

        String receiver = "you";
        if (firstDiscordID != e.getAuthor().getIdLong()) {
            receiver = getUserString(e, firstDiscordID);
        }
        DiscordUserDisplay giverUI = CommandUtil.getUserInfoConsideringGuildOrNot(e, secondDiscordID);
        String giver = giverUI.getUsername();

        if (recs.isEmpty()) {
            sendMessageQueue(e, String.format("Couldn't get %s any recommendation from %s", receiver, giver));
        } else {
            if (recs.size() == 1) {
                sendMessageQueue(e, String.format("**%s** has recommended %s to listen to **%s** (they have %d plays)",
                        giver, receiver, CommandUtil.cleanMarkdownCharacter(recs.get(0).getArtist()), recs.get(0).getCount()));
                getService().insertRecommendation(secondDiscordID, firstDiscordID, recs.get(0).getArtistId());
            } else {
                int artistNum = 1;
                StringBuilder s = new StringBuilder();
                for (ScrobbledArtist rec : recs) {
                    s.append((String.format("%d. [%s](%s): %d plays%n", artistNum, CommandUtil.cleanMarkdownCharacter(rec.getArtist()), CommandUtil.getLastFmArtistUrl(rec.getArtist()), rec.getCount())));
                    artistNum++
                }
                EmbedBuilder embedBuilder = new EmbedBuilder();

                embedBuilder.setTitle(String.format("%s recommendations for %s", giver, receiver))
                        .setThumbnail(giverUI.getUrlImage())
                        .setDescription(s);
                new MessageBuilder(embedBuilder.build()).sendTo(e.getChannel()).queue();
            }
        }
    }
}
