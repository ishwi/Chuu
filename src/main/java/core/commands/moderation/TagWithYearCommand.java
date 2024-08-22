package core.commands.moderation;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ButtonUtils;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.Confirmator;
import core.otherlisteners.Reactions;
import core.otherlisteners.util.ConfirmatorItem;
import core.parsers.AlbumYearParser;
import core.parsers.Parser;
import core.parsers.params.AlbumYearParameters;
import core.util.ServiceView;
import dao.entities.AlbumInfo;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.time.Year;
import java.util.List;

import static core.otherlisteners.Reactions.ACCEPT;
import static core.otherlisteners.Reactions.REJECT;


public class TagWithYearCommand extends ConcurrentCommand<AlbumYearParameters> {


    public TagWithYearCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<AlbumYearParameters> initParser() {
        return new AlbumYearParser(db);
    }

    @Override
    public String getDescription() {
        return "Submit an album and its release year so it can be seen in the aoty command";
    }

    @Override
    public List<String> getAliases() {
        return List.of("albumyear", "aly");
    }

    @Override
    public String getName() {
        return "Album Year";
    }

    @Override
    public void onCommand(Context e, @NotNull AlbumYearParameters params) throws InstanceNotFoundException {

        Year y = params.getYear();
        AlbumInfo ai = params.getAlbumInfo();
        List<AlbumInfo> found = db.albumsOfYear(List.of(ai), y);

        String album = params.getAlbumInfo().getName();
        String artist = params.getAlbumInfo().getArtist();
        if (!found.isEmpty()) {
            parser.sendError(String.format("**%s** - **%s** was already tagged as a **%s** album!", artist, album, y), e);
            return;
        }

        long discordId = e.getAuthor().getIdLong();
        LastFMData lastFMData = db.findLastFMData(discordId);
        String userString = getUserString(e, discordId);
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setTitle("Year confirmation")
                .setDescription(String.format("%s, want to tag the album **%s** of **%s** with the year **%s**?", userString, album, artist, y));
        List<ConfirmatorItem> items = List.of(
                new ConfirmatorItem(Reactions.ACCEPT, who -> {
            if (lastFMData.getRole() == Role.ADMIN) {
                return who.clear().setTitle(String.format("**%s** - **%s** was tagged as a **%s** album", artist, album, y)).setColor(CommandUtil.pastelColor());
            } else {
                return who.clear().setTitle("Your submission was passed to the mod team").setColor(CommandUtil.pastelColor());
            }
        }, (z) -> {
            if (lastFMData.getRole() == Role.ADMIN) {
                db.insertAlbumOfYear(ai, y);
            } else {
                TextChannel textChannelById = Chuu.getShardManager().getTextChannelById(Chuu.channelId);
                if (textChannelById != null)
                    textChannelById.sendMessageEmbeds(new ChuuEmbedBuilder(e).setTitle("Year submission")
                                    .setColor(CommandUtil.pastelColor())
                                    .setDescription("Artist: **%s**\nAlbum: **%s**\nYear: **%s**\nAuthor: %s".formatted(artist, album, y, e.getAuthor().getAsMention())).build())
                            .setComponents(ActionRow.of(
                                            Button.of(ButtonStyle.PRIMARY, ACCEPT, "Accept", Emoji.fromUnicode(ACCEPT)),
                                            Button.of(ButtonStyle.DANGER, REJECT, "Reject", Emoji.fromUnicode(REJECT))
                                    )
                            )
                            .queue();
            }
                }), new ConfirmatorItem(Reactions.REJECT,
                        who -> who.clear().setTitle(String.format("Didn't tag %s - %s", artist, album)).setColor(CommandUtil.pastelColor()),
                (z) -> {
                }));

        ActionRow row = ActionRow.of(ButtonUtils.primary("Submit"), ButtonUtils.danger("Cancel"));

        e.sendMessage(embedBuilder.build(), row)
                .queue(mes -> new Confirmator(embedBuilder, e, mes, discordId, items));
    }
}
