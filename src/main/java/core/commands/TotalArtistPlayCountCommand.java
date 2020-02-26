package core.commands;

import core.otherlisteners.Reactionary;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.ResultWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.stream.Collectors;

public class TotalArtistPlayCountCommand extends ResultWrappedCommand<ArtistPlays> {

    public TotalArtistPlayCountCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    public ResultWrapper<ArtistPlays> getList(String[] message, MessageReceivedEvent e) {
        return getService().getArtistPlayCount(e.getGuild().getIdLong());
    }

    @Override
    public void printList(ResultWrapper<ArtistPlays> wrapper, MessageReceivedEvent e) {
        List<ArtistPlays> list = wrapper.getResultList();
        if (list.size() == 0) {
            sendMessageQueue(e, "No one has played any artist yet!");
            return;
        }

        StringBuilder a = new StringBuilder();
        List<String> collect = list.stream().map(x -> ". [" +
                                                      x.getArtistName() +
                                                      "](" + CommandUtil.getLastFmArtistUrl(x.getArtistName()) +
                                                      ") - " + x.getCount() +
                                                      " plays \n").collect(Collectors.toList());
        EmbedBuilder embedBuilder = initList(a, collect)
                .setTitle("Total artist plays")
                .setFooter(e.getGuild().getName() + " has " + wrapper.getRows() + " total plays!\n", null)
                .setThumbnail(e.getGuild().getIconUrl());
        MessageBuilder mes = new MessageBuilder();
        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                executor.execute(() -> new Reactionary<>(collect, message1, embedBuilder)));
    }


    @Override
    public String getDescription() {
        return "Total Plays";
    }

    @Override
    public List<String> getAliases() {
        return List.of("totalplays", "tp");
    }

    @Override
    public String getName() {
        return "Total Plays";
    }
}
