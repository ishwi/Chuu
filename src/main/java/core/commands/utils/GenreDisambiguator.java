package core.commands.utils;

import com.google.common.collect.Lists;
import core.commands.Context;
import core.otherlisteners.Confirmator;
import core.otherlisteners.util.ConfirmatorItem;
import dao.ChuuService;
import dao.entities.QuadConsumer;
import dao.everynoise.NoiseGenre;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class GenreDisambiguator {
    private final ChuuService db;

    public GenreDisambiguator(ChuuService db) {
        this.db = db;
    }

    public <T> void disambiguate(Context e, String input, Function<NoiseGenre, T> mapper, QuadConsumer<Context, Message, T, String> succesFunction) {
        Optional<NoiseGenre> exactMatch = db.findExactMatch(input);
        NoiseGenre theOne;
        if (exactMatch.isEmpty()) {
            List<NoiseGenre> matchingGenre = db.findMatchingGenre(input).stream().limit(9).toList();
            if (matchingGenre.isEmpty()) {
                e.sendMessageQueue("Couldn't find any genre searching by " + input);
                return;
            } else if (matchingGenre.size() > 1) {
                int counter = 1;
                List<ConfirmatorItem> reacts = new ArrayList<>();
                StringBuilder description = new StringBuilder();
                List<Component> buttons = new ArrayList<>();
                var eb = new ChuuEmbedBuilder(e).setTitle("Multiple genres found");
                for (NoiseGenre noiseGenre : matchingGenre) {
                    //  48 is 0x0030 -> which is 0 || 0x0031 is 1 ...
                    String s = new String(new int[]{48 + counter++}, 0, 1);
                    String emote = s + "\ufe0f\u20e3";

                    ConfirmatorItem confirmatorItem = new ConfirmatorItem(emote, (z) -> z, (z) ->
                            succesFunction.apply(e, z, mapper.apply(noiseGenre), noiseGenre.name()));
                    reacts.add(confirmatorItem);
                    if (!e.isFromGuild()) {
                        description.append(emote).append(" ➜ ").append(noiseGenre.name()).append("\n");
                    }
                    buttons.add(Button.primary(emote, noiseGenre.name()));
                }
                List<ActionRow> rows = Lists.partition(buttons, 5).stream().map(ActionRow::of).toList();
                if (!e.isFromGuild()) {
                    eb.setFooter("Please disambiguate choosing the appropiate emote");
                } else {
                    eb.setFooter("Please disambiguate choosing the appropiate button");

                }
                e.sendMessage(eb.setDescription(description).build(), rows)
                        .queue(message -> new Confirmator(eb, e, message, e.getAuthor().getIdLong(), reacts, (z) ->
                                z.clear().setDescription("You didn't select any genre!").setColor(CommandUtil.pastelColor()), false, 50));
                return;
            } else
                theOne = matchingGenre.get(0);
        } else {
            theOne = exactMatch.get();
        }
        succesFunction.apply(e, null, mapper.apply(theOne), theOne.name());
    }


}
