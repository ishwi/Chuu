package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
import dao.ChuuService;
import dao.entities.LbEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class MatchingArtistCommand extends ConcurrentCommand {


    public MatchingArtistCommand(ChuuService dao) {
        super(dao);
        this.parser = new OnlyUsernameParser(dao);
        this.respondInPrivate = false;
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
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] message = parser.parse(e);
        if (message == null) {
            return;
        }

        long discordId = Long.parseLong(message[1]);
        List<LbEntry> list = getService().matchingArtistsCount(discordId, e.getGuild().getIdLong());
        list.forEach(cl -> cl.setDiscordName(getUserString(cl.getDiscordId(), e, cl.getLastFmId())));
        MessageBuilder messageBuilder = new MessageBuilder();
        StringBuilder url = new StringBuilder();
        StringBuilder usableName = new StringBuilder();

        CommandUtil.getUserInfoConsideringGuildOrNot(usableName, url, e, discordId);

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(url.toString());
        StringBuilder a = new StringBuilder();

        if (list.isEmpty()) {
            sendMessageQueue(e, "No one has any matching artist with you :(");
            return;
        }

        for (int i = 0; i < 10 && i < list.size(); i++) {
            a.append(i + 1).append(list.get(i).toString());
        }
        embedBuilder.setDescription(a).setTitle("Matching artists with " + usableName.toString())
                .setFooter(usableName + " has " + getService().getUserArtistCount(message[0]) + " total artist!\n", null);
        messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue(mes ->
                executor.execute(() -> new Reactionary<>(list, mes, embedBuilder)));
    }
}
