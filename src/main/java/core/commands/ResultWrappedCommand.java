package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import dao.ChuuService;
import dao.entities.ResultWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public abstract class ResultWrappedCommand<T> extends ConcurrentCommand {

    ResultWrappedCommand(ChuuService dao) {
        super(dao);
        this.parser = new NoOpParser();
    }


    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] parse = this.parser.parse(e);
        if (parse == null) {
            return;
        }
        printList(getList(parse, e), e);
    }

    public EmbedBuilder initList(List<String> collect) {
        StringBuilder a = new StringBuilder();
        for (int i = 0, size = collect.size(); i < 10 && i < size; i++) {
            String text = collect.get(i);
            a.append(i + 1).append(text);
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(a);
        return embedBuilder.setColor(CommandUtil.randomColor());
    }


    public abstract ResultWrapper<T> getList(String[] message, MessageReceivedEvent e) throws LastFmException;

    public abstract void printList(ResultWrapper<T> list, MessageReceivedEvent e);

}
