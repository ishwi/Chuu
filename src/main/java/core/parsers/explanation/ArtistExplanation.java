package core.parsers.explanation;

import core.Chuu;
import core.parsers.explanation.util.Autocompletable;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineTypeAutocomplete;
import core.parsers.explanation.util.Interactible;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ArtistExplanation implements Explanation {
    public static final String NAME = "artist";

    @Override
    public Interactible explanation() {
        return new ExplanationLineTypeAutocomplete(NAME, "The artist written will be used or if no artist is provided the artist that you are currently listening to will be used", OptionType.STRING, this::searchChoices);
    }

    public List<Command.Choice> searchChoices(CommandAutoCompleteInteractionEvent e) {
        String search = e.getFocusedOption().getValue();

        ChuuService db = Chuu.getDb();
        if (StringUtils.isBlank(search)) {
            List<ScrobbledArtist> allUserArtist = db.getAllUserArtist(e.getUser().getIdLong(), 25);
            if (allUserArtist.isEmpty()) {
                return db.searchArtists(search, 25).stream().map(Autocompletable::of).toList();
            }
            return allUserArtist.stream().map(x -> Autocompletable.of(x.getArtist())).toList();
        }
        return db.searchArtists(search, 25).stream().map(Autocompletable::of).toList();
    }
}
