package core.commands.utils;

import core.commands.Context;
import core.otherlisteners.Confirmator;
import core.otherlisteners.util.ConfirmatorItem;
import dao.ChuuService;
import dao.entities.QuadConsumer;
import dao.everynoise.NoiseGenre;
import net.dv8tion.jda.api.entities.Message;

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
            List<NoiseGenre> matchingGenre = db.findMatchingGenre(input).stream().limit(7).toList();
            if (matchingGenre.isEmpty()) {
                e.sendMessageQueue("Couldn't find any genre searching by " + input);
                return;
            } else if (matchingGenre.size() > 1) {
                int counter = 1;
                List<ConfirmatorItem> reacts = new ArrayList<>();
                StringBuilder description = new StringBuilder();
                var eb = new ChuuEmbedBuilder(e).setTitle("Multiple genres found").setFooter("Please disambiguate choosing the appropiate emote");
                for (NoiseGenre noiseGenre : matchingGenre) {
                    //  48 is 0x0030 -> which is 0 || 0x0031 is 1 ...
                    String s = new String(new int[]{48 + counter++}, 0, 1);
                    String emote = s + "\ufe0f\u20e3";

                    ConfirmatorItem confirmatorItem = new ConfirmatorItem(emote, (z) -> z, (z) ->
                            succesFunction.apply(e, z, mapper.apply(noiseGenre), noiseGenre.name()));
                    reacts.add(confirmatorItem);
                    description.append(emote).append(" \u279C ").append(noiseGenre.name()).append("\n");
                }
                e.sendMessage(eb.setDescription(description).build())
                        .queue(message -> new Confirmator(eb, message, e.getAuthor().getIdLong(), reacts, (z) -> z.clear().setDescription("You didn't select any genre!"), false, 50));
                return;
            } else
                theOne = matchingGenre.get(0);
        } else {
            theOne = exactMatch.get();
        }
        succesFunction.apply(e, null, mapper.apply(theOne), theOne.name());
    }


}
