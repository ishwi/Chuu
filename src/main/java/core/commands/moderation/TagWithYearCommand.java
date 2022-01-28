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
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import dao.entities.AlbumInfo;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import javax.annotation.Nonnull;
import java.time.Year;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static core.otherlisteners.Reactions.ACCEPT;
import static core.otherlisteners.Reactions.REJECT;


public class TagWithYearCommand extends ConcurrentCommand<CommandParameters> {


    public TagWithYearCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
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
    public void onCommand(Context e, @Nonnull CommandParameters params) throws InstanceNotFoundException {
        long idLong = e.getAuthor().getIdLong();
        Pattern a = Pattern.compile(".*(?:year|y):(\\d{4}).*");
        // Check if it exists
        String[] subMessage = parser.getSubMessage(e);
        String message = String.join(" ", subMessage);
        Matcher matcher = a.matcher(message);
        if (!matcher.matches()) {
            parser.sendError(String.format("Invalid format. You must provide the artist name and then the year with the following format: artist - album year:%s", Year.now()), e);
            return;
        }
        LastFMData lastFMData = db.findLastFMData(e.getAuthor().getIdLong());
        String year = matcher.group(1);
        Year parse;
        try {
            parse = Year.parse(year);
            if (parse.isAfter(Year.now())) {
                parser.sendError(year + " is a future year and cannot be used.", e);
                return;
            }
        } catch (
                java.time.format.DateTimeParseException exception) {
            parser.sendError(String.format("Invalid format. You must provide the artist name and then the year with the following format: artist - album year:%s", Year.now()), e);
            return;
        }
        message = message.replaceFirst("(y|year):" + year, "");
        String regex = "(?<!\\\\)" + ("\\s*-\\s*");
        String[] content = message.split(regex);

        if (content.length != 2) {
            parser.sendError(String.format("Invalid format. You must provide the artist name and then the year with the following format: artist - album year:%s", Year.now()), e);
            return;
        }
        String artist = content[0].trim().replaceAll("\\\\-", "-");
        String album = content[1].trim().replaceAll("\\\\-", "-");

        AlbumInfo ai = new AlbumInfo(album, artist);
        List<AlbumInfo> found = db.albumsOfYear(List.of(ai), parse);

        if (!found.isEmpty()) {
            parser.sendError(String.format("**%s** - **%s** was already tagged as a **%s** album!", artist, album, year), e);
            return;
        }

        String userString = getUserString(e, idLong);
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setTitle("Year confirmation")
                .setDescription(String.format("%s, want to tag the album **%s** of **%s** with the year **%s**?", userString, album, artist, year));
        List<ConfirmatorItem> items = List.of(new ConfirmatorItem(Reactions.ACCEPT, who -> {
            if (lastFMData.getRole() == Role.ADMIN) {
                return who.clear().setTitle(String.format("**%s** - **%s** was tagged as a **%s** album", artist, album, year)).setColor(CommandUtil.pastelColor());
            } else {
                return who.clear().setTitle("Your submission was passed to the mod team").setColor(CommandUtil.pastelColor());
            }
        }, (z) -> {
            if (lastFMData.getRole() == Role.ADMIN) {
                db.insertAlbumOfYear(ai, parse);
            } else {
                TextChannel textChannelById = Chuu.getShardManager().getTextChannelById(Chuu.channelId);
                if (textChannelById != null)
                    textChannelById.sendMessageEmbeds(new ChuuEmbedBuilder(e).setTitle("Year submission")
                                    .setColor(CommandUtil.pastelColor())
                                    .setDescription("Artist: **%s**\nAlbum: **%s**\nYear: **%s**\nAuthor: %s".formatted(artist, album, year, e.getAuthor().getAsMention())).build())
                            .setActionRows(ActionRow.of(
                                            Button.of(ButtonStyle.PRIMARY, ACCEPT, "Accept", Emoji.fromUnicode(ACCEPT)),
                                            Button.of(ButtonStyle.DANGER, REJECT, "Reject", Emoji.fromUnicode(REJECT))
                                    )
                            )
                            .queue();
            }
        }), new ConfirmatorItem(Reactions.REJECT, who -> who.clear().setTitle(String.format("Didn't tag %s - %s", artist, album)).setColor(CommandUtil.pastelColor()),
                (z) -> {
                }));

        ActionRow row = ActionRow.of(ButtonUtils.primary("Submit"), ButtonUtils.danger("Cancel"));

        e.sendMessage(embedBuilder.build(), row)
                .queue(mes -> new Confirmator(embedBuilder, e, mes, idLong, items));
    }
}
