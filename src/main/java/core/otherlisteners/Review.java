package core.otherlisteners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

public interface Review<T> {

    T elementFetcher();

    EmbedBuilder fillBuilder(T elem, EmbedBuilder embedBuilder);

    void succesFunction(T elem, JDA jda);

    void rejectFunction(T elem);

}


