package core.commands;

import core.otherlisteners.Reactionary;
import core.parsers.NoOpParser;
import dao.ChuuService;
import dao.entities.ArtistPlays;
import dao.entities.ResultWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArtistFrequencyCommand extends ResultWrappedCommand<ArtistPlays> {

    public ArtistFrequencyCommand(ChuuService dao) {
        super(dao);
        this.parser = new NoOpParser();
        this.respondInPrivate = false;
    }

    @Override
    public ResultWrapper<ArtistPlays> getList(String[] message, MessageReceivedEvent e) {
        return getService().getArtistFrequencies(e.getGuild().getIdLong());
    }

    @Override
    public void printList(ResultWrapper<ArtistPlays> wrapper, MessageReceivedEvent e) {
        List<ArtistPlays> list = wrapper.getResultList();
        if (list.size() == 0) {
            sendMessageQueue(e, "No one has played any artist yet!");
        }

        List<String> collect = list.stream().map(x -> ". [" +
                                                      x.getArtistName() +
                                                      "](" + CommandUtil.getLastFmArtistUrl(x.getArtistName()) +
                                                      ") - " + x.getCount() +
                                                      " listeners \n").collect(Collectors.toList());
        EmbedBuilder embedBuilder = initList(collect);
        embedBuilder.setTitle("Artist's frequencies");
        embedBuilder.setFooter(e.getGuild().getName() + " has " + wrapper.getRows() + " different artists!\n", null);
        embedBuilder.setThumbnail(e.getGuild().getIconUrl());
        MessageBuilder mes = new MessageBuilder();
        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                executor.execute(() -> new Reactionary<>(collect, message1, embedBuilder)));
    }

    @Override
    public String getDescription() {
        return "Artist ordered by listener";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("listeners", "frequencies", "hz");
    }

    @Override
    public String getName() {
        return "Artist Listeners";
    }
}
