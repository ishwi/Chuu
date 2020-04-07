package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.RecommendationParser;
import core.parsers.params.RecommendationsParams;
import dao.ChuuService;
import dao.entities.Affinity;
import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;

public class RecommendationCommand extends ConcurrentCommand {
    public RecommendationCommand(ChuuService dao) {
        super(dao);
        this.parser = new RecommendationParser(dao);
        this.respondInPrivate = false;
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
        String[] parse = parser.parse(e);
        if (parse == null) {
            return;
        }
        RecommendationsParams rp = new RecommendationsParams(parse, e);
        long firstDiscordID;
        long secondDiscordID;
        if (rp.isNoUser()) {
            LastFMData lastFMData = getService().findLastFMData(e.getAuthor().getIdLong());
            List<Affinity> serverAffinity = getService().getServerAffinity(lastFMData.getName(), e.getGuild().getIdLong(), AffinityCommand.DEFAULT_THRESHOLD);
            if (serverAffinity.isEmpty()) {
                sendMessage(e, "Couldn't get you any recommendation :(");
                return;
            }
            Affinity affinity = serverAffinity.get(0);
            firstDiscordID = e.getAuthor().getIdLong();
            secondDiscordID = affinity.getDiscordId();
        } else {
            firstDiscordID = rp.getFirstDiscordID();
            secondDiscordID = rp.getSecondDiscordID();
        }
        Optional<ScrobbledArtist> recommendation = getService().getRecommendation(secondDiscordID, firstDiscordID, rp.isShowRepeated());

        String receiver = "you";
        if (firstDiscordID != e.getAuthor().getIdLong()) {
            receiver = getUserString(e, firstDiscordID);
        }
        String giver = CommandUtil.getUserInfoConsideringGuildOrNot(e, secondDiscordID).getUsername();

        if (recommendation.isEmpty()) {
            sendMessage(e, String.format("Couldn't get %s any recommendation from %s", receiver, giver));
        } else {
            sendMessageQueue(e, String.format("**%s** has recommended %s to listen to **%s** (they have %d plays)", giver, receiver, CommandUtil.cleanMarkdownCharacter(recommendation.get().getArtist()), recommendation.get().getCount()));
            getService().insertRecommendation(secondDiscordID, firstDiscordID, recommendation.get().getArtistId());
        }
    }
}
