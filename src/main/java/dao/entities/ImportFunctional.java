package dao.entities;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

@FunctionalInterface
public interface ImportFunctional {
    Callback executeCallback(LastFMData a, StringBuilder b, Message message, EmbedBuilder embedBuilder, User user, int position, int[] errorCounter);

}
