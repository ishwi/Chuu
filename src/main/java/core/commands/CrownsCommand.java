package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.DiscordUserDisplay;
import dao.entities.UniqueWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class CrownsCommand extends ConcurrentCommand {
    public CrownsCommand(ChuuService dao) {
        super(dao);
        parser = new OnlyUsernameParser(dao);
        this.respondInPrivate = false;

    }

    public boolean isGlobal() {
        return false;
    }

    public UniqueWrapper<ArtistPlays> getList(long guildId, String lastFmName) {
        return getService().getCrowns(lastFmName, guildId);
    }

    @Override
    public String getDescription() {
        return ("List of artist you are the top listener within a server");
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("crowns");
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] returned = parser.parse(e);
        String lastFmName = returned[0];
        //long discordID = Long.parseLong(returned[1]);
        UniqueWrapper<ArtistPlays> uniqueDataUniqueWrapper = getList(e.getGuild().getIdLong(), lastFmName);
        DiscordUserDisplay userInformation = CommandUtil.getUserInfoConsideringGuildOrNot(e, uniqueDataUniqueWrapper.getDiscordId());
        String userName = userInformation.getUsername();
        String userUrl = userInformation.getUrlImage();
        List<ArtistPlays> resultWrapper = uniqueDataUniqueWrapper.getUniqueData();

        int rows = resultWrapper.size();
        if (rows == 0) {
            sendMessageQueue(e, userName + " doesn't have any" + (isGlobal() ? " global " : " ") + "crown :'(");
            return;
        }

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < rows; i++) {
            ArtistPlays g = resultWrapper.get(i);
            a.append(i + 1).append(g.toString());
        }


        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(a);
        embedBuilder.setColor(CommandUtil.randomColor());
        embedBuilder.setTitle(userName + "'s " + (isGlobal() ? "global " : "") + "crowns", CommandUtil.getLastFmUser(uniqueDataUniqueWrapper.getLastFmId()));
        embedBuilder.setFooter(userName + " has " + resultWrapper.size() + (isGlobal() ? " global" : "") + " crowns!!\n", null);
        embedBuilder.setThumbnail(userUrl);

        MessageBuilder mes = new MessageBuilder();
        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->

                executor.execute(() -> new Reactionary<>(resultWrapper, message1, embedBuilder)));
    }

    @Override
    public String getName() {
        return "Crowns";
    }


}



