package core.commands.moderation;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.Validator;
import core.parsers.ArtistAlbumParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.services.AlbumValidator;
import dao.ChuuService;
import dao.entities.ScrobbledAlbum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class AlbumAltCovers extends ConcurrentCommand<ArtistAlbumParameters> {
    private static final String RIGHT_ARROW = "U+27a1";
    private static final String LEFT_ARROW = "U+2b05";
    private final BiFunction<JDA, Integer, BiFunction<String, EmbedBuilder, EmbedBuilder>> builder = (jda, integer) -> (votingEntity, embedBuilder) ->
            embedBuilder.clearFields()
                    .setImage(CommandUtil.noImageUrl(votingEntity))
                    .setColor(CommandUtil.pastelColor());

    public AlbumAltCovers(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        return new ArtistAlbumParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "List the alternate covers for an album";
    }

    @Override
    public List<String> getAliases() {
        return List.of("altalbumcover", "albumalt");
    }

    @Override
    public String getName() {
        return "Album alt covers";
    }

    @Override
    protected void onCommand(Context e, @NotNull ArtistAlbumParameters params) throws LastFmException {
        ScrobbledAlbum album = new AlbumValidator(db, lastFM).validate(params.getArtist(), params.getAlbum());
        List<String> covers = Chuu.getCoverService().getCovers(album.getAlbumId());
        if (covers.isEmpty()) {
            sendMessageQueue(e, "**%s - %s** has no alt covers".formatted(params.getArtist(), params.getAlbum()));
            return;
        }

        HashMap<String, BiFunction<String, MessageReactionAddEvent, Boolean>> actionMap = new HashMap<>();
        AtomicInteger counter = new AtomicInteger(0);

        if (covers.size() > 1) {
            actionMap.put(LEFT_ARROW, (aliasEntity, r) -> {
                int i = counter.decrementAndGet();
                if (i == 0) {
                    r.getReaction().clearReactions().queue();
                }
                if (i == covers.size() - 2) {
                    r.getChannel().addReactionById(r.getMessageIdLong(), RIGHT_ARROW).queue();
                }
                return false;
            });
            actionMap.put(RIGHT_ARROW, (a, r) -> {
                int i = counter.incrementAndGet();
                if (i == covers.size() - 1) {
                    r.getReaction().clearReactions().queue();
                }
                if (i == 1) {
                    r.getChannel().addReactionById(r.getMessageIdLong(), LEFT_ARROW).queue();
                }
                return false;
            });
        }

        new Validator<>(
                finalEmbed -> finalEmbed,

                () -> {
                    if (counter.get() >= covers.size() - 1) {
                        counter.set(covers.size() - 1);
                    }
                    if (counter.get() < 0) {
                        counter.set(0);
                    }
                    return covers.get(counter.get());
                },
                builder.apply(e.getJDA(), covers.size())
                , new ChuuEmbedBuilder(e).setTitle("%s - %s".formatted(params.getArtist(), params.getAlbum())), e, e.getAuthor().getIdLong(), actionMap, true, true);
    }
}
