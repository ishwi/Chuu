package core.commands;

import core.otherlisteners.Reactionary;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.ResultWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GlobalTotalArtistPlaysCountCommand extends ResultWrappedCommand<ArtistPlays> {
    public GlobalTotalArtistPlaysCountCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public ResultWrapper<ArtistPlays> getList(String[] message, MessageReceivedEvent e) {
        return getService().getArtistPlayCountGlobal();
    }

    @Override
    public void printList(ResultWrapper<ArtistPlays> list, MessageReceivedEvent e) {
        if (list.getRows() == 0) {
            sendMessageQueue(e, "No one has ever played any artist!");
            return;
        }

        StringBuilder a = new StringBuilder();
        List<ArtistPlays> resultList = list.getResultList();

        List<String> collect = resultList.stream().map(x -> ". [" +
                                                            x.getArtistName() +
                                                            "](" + CommandUtil.getLastFmArtistUrl(x.getArtistName()) +
                                                            ") - " + x.getCount() +
                                                            " plays\n").collect(Collectors.toList());
        EmbedBuilder embedBuilder = initList(collect)
                .setTitle("Most Played Artists")
                .setFooter(e.getJDA().getSelfUser().getName() + " has stored " + list.getRows() + " plays!\n", null)
                .setThumbnail(e.getJDA().getSelfUser().getAvatarUrl());
        MessageBuilder mes = new MessageBuilder();
        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                executor.execute(() -> new Reactionary<>(collect, message1, embedBuilder)));
    }

    @Override
    public String getDescription() {
        return " Artists ranked by total plays on all servers that this bot handles";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("globalplays", "gp");
    }

    @Override
    public String getName() {
        return "Total Artist Plays";
    }
}
