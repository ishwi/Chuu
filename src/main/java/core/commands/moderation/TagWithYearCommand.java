package core.commands.moderation;

import core.Chuu;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.otherlisteners.Confirmator;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.time.Year;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagWithYearCommand extends ConcurrentCommand<CommandParameters> {


    public TagWithYearCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return new NoOpParser();
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
    protected void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) throws InstanceNotFoundException {
        long idLong = e.getAuthor().getIdLong();
        Pattern a = Pattern.compile(".*(?:year|y):(\\d{4}).*");
        // Check if it exists
        String[] subMessage = parser.getSubMessage(e.getMessage());
        String message = String.join(" ", subMessage);
        Matcher matcher = a.matcher(message);
        if (!matcher.matches()) {
            parser.sendError(String.format("Invalid format. You must provide the artist name and then the year with the following format: artist - album year:%s", Year.now().toString()), e);
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
            parser.sendError(String.format("Invalid format. You must provide the artist name and then the year with the following format: artist - album year:%s", Year.now().toString()), e);
            return;
        }
        message = message.replaceFirst("(y|year):" + year, "");
        String regex = "(?<!\\\\)" + ("\\s*-\\s*");
        String[] content = message.split(regex);

        if (content.length != 2) {
            parser.sendError(String.format("Invalid format. You must provide the artist name and then the year with the following format: artist - album year:%s", Year.now().toString()), e);
            return;
        }
        String artist = content[0].trim().replaceAll("\\\\-", "-");
        String album = content[1].trim().replaceAll("\\\\-", "-");
        String userString = getUserString(e, idLong);
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Year confirmation")
                .setDescription(String.format("%s, want to tag the album **%s** of **%s** with the year **%s**?", userString, album, artist, year));
        e.getChannel().sendMessage(new MessageBuilder(embedBuilder.build()).build())
                .queue(queu -> new Confirmator(embedBuilder, queu, idLong,
                        () -> {
                            if (lastFMData.getRole() == Role.ADMIN) {
                                db.insertAlbumsOfYear(List.of(new AlbumInfo(album, artist)), parse);
                            } else {
                                TextChannel textChannelById = Chuu.getShardManager().getTextChannelById(Chuu.channelId);
                                if (textChannelById != null)
                                    textChannelById.sendMessage(new EmbedBuilder().setTitle("Year submission")
                                            .setDescription("Artist: **" + artist + "**\nAlbum: **" + album + "**\nYear: **" + year + "**\nAuthor: " + e.getAuthor().getIdLong()).build()).flatMap(q ->
                                            q.addReaction("U+2714").flatMap(t -> q.addReaction("U+274c"))
                                    ).queue();
                            }
                        }, () -> {
                },
                        who -> {
                            if (lastFMData.getRole() == Role.ADMIN) {
                                return who.clear().setTitle(String.format("%s - %s was tagged as a %s album", artist, album, year));
                            } else {
                                return who.clear().setTitle("Your submission was passed to the mod team");
                            }
                        },
                        who -> who.clear().setTitle(String.format("Didn't tag %s - %s", artist, album))));
    }
}
