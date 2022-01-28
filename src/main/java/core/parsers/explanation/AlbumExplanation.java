package core.parsers.explanation;

import core.Chuu;
import core.parsers.explanation.util.Autocompletable;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineTypeAutocomplete;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public record AlbumExplanation(Explanation artist, Explanation album) {
    public static final String NAME = "album";


    public AlbumExplanation() {
        this(
                new ArtistExplanation(),
                () -> new ExplanationLineTypeAutocomplete(NAME, "The album of the artist to query for, separated with -", OptionType.STRING, AlbumExplanation::searchChoices));

    }

    public static List<Command.Choice> searchChoices(CommandAutoCompleteInteractionEvent e) {
        String search = e.getFocusedOption().getValue();
        OptionMapping option = e.getOption(ArtistExplanation.NAME);

        ChuuService db = Chuu.getDb();
        Optional<String> opt = Optional.ofNullable(option).map(OptionMapping::getAsString).filter(Predicate.not(StringUtils::isBlank));
        if (opt.isEmpty()) {
            return nonExistingArtist(search, db, e);
        } else {
            String artist = opt.get();
            try {
                long artistId = db.getArtistId(artist);
                if (StringUtils.isBlank(search)) {
                    return db.getGlobalTopArtistAlbums(25, artistId).stream().map(x -> Autocompletable.of(x.getAlbum())).toList();
                } else {
                    return db.searchAlbumsForArtist(search, artistId, 25).stream().map(Autocompletable::of).toList();
                }
            } catch (InstanceNotFoundException ex) {
                return nonExistingArtist(search, db, e);
            }
        }
    }


    private static List<Command.Choice> nonExistingArtist(String search, ChuuService db, CommandAutoCompleteInteractionEvent e) {
        if (StringUtils.isBlank(search)) {
            return getUserTopAlbums(db, e);
        } else {
            return db.searchAlbums(search, 25).stream().map(Autocompletable::of).toList();
        }
    }


    private static List<Command.Choice> getUserTopAlbums(ChuuService db, CommandAutoCompleteInteractionEvent e) {
        try {
            LastFMData data = db.findLastFMData(e.getUser().getIdLong());
            return db.getUserAlbums(data.getName(), 25).stream().map(x -> Autocompletable.of(x.getAlbum())).toList();
        } catch (InstanceNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}

