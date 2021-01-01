package core.commands.rym;

import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.UrlParser;
import core.parsers.params.UrlParameters;
import dao.ChuuService;
import dao.entities.RYMImportRating;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.text.StringEscapeUtils;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URL;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RYMDumpImportCommand extends ConcurrentCommand<UrlParameters> {
    private static final String headerLine = "RYM Album, First Name,Last Name,First Name localized, Last Name localized,Title,Release_Date,Rating,Ownership,Purchase Date,Media Type,Review";
    private static final Pattern unlocalized = Pattern.compile("(.*) \\[(.*)] ?");
    private static final Set<Long> usersInProcess = new HashSet<>();
    private static final Function<String, RYMImportRating> mapper = (line) -> {
        String[] split = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (split.length != 12)
            return null;
        split = Arrays.stream(split).map(x -> {
            x = StringEscapeUtils.unescapeHtml4(x.substring(1, x.length() - 1));
            int i = x.indexOf('/');
            if (i != -1 && (i > 0.2 * x.length() || i < 0.9 * x.length())) {
                x = x.split("/")[0];
            }
            return x;
        }).toArray(String[]::new);
        try {
            long rymId = Long.parseLong(split[0]);
            String firstName = split[1];
            String lastName = split[2];
            String firstNameLocalized = split[3];
            String lastNameLocalized = split[4];
            String title = split[5];
            Year year = split[6].isEmpty() ? null : Year.parse(split[6]);
            Byte rating = Byte.valueOf(split[7]);
            boolean ownership = split[8].equals("Y");
            Year purchaseDate = split[9].isEmpty() ? null : Year.parse(split[9]);
            String mediaType = split[10];
            String review = split[11];
            Matcher matcher;
            if (firstName.isBlank() && firstNameLocalized.isBlank() && lastNameLocalized.isBlank() && (matcher = unlocalized.matcher(lastName)).matches()) {
                String group = matcher.group(1);
                String group1 = matcher.group(2);
                lastName = group;
                lastNameLocalized = group1;
            }
            return new RYMImportRating(rymId, firstName, lastName, firstNameLocalized, lastNameLocalized, title, year, rating, ownership, purchaseDate, mediaType, review);
        } catch (NumberFormatException | DateTimeParseException ex) {
            return null;
        }
    };

    public RYMDumpImportCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.RYM_BETA;
    }

    @Override
    public Parser<UrlParameters> initParser() {
        return new UrlParser(false);
    }

    @Override
    public String getDescription() {
        return "Load you rym rating into the bot. Read the help message for info about how to do it";
    }

    @Override
    public List<String> getAliases() {
        return List.of("rymimport");
    }

    @Override
    public String getName() {
        return "RYM Import";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull UrlParameters params) throws LastFmException, InstanceNotFoundException {


        List<RYMImportRating> ratings = new ArrayList<>();
        String url = params.getUrl();
        if (url.isBlank()) {
            sendMessageQueue(e, "You need to upload a file :thinking:");
            return;
        }
        Pattern compile = Pattern.compile("https?://rateyourmusic.com/");
        if (compile.matcher(url).matches()) {
            sendMessageQueue(e, "You can't link directly to the export page, you should download the file and upload it to discord");
            return;
        }
        try {
            try {
                synchronized (usersInProcess) {
                    if (usersInProcess.contains(e.getAuthor().getIdLong())) {
                        sendMessageQueue(e, "Your previous import command is still being processed");
                        return;
                    }
                    usersInProcess.add(e.getAuthor().getIdLong());
                }

                URL url1 = new URL(url);
                Scanner s = new Scanner(url1.openStream());
                if (!s.hasNextLine()) {
                    sendMessageQueue(e, "File was empty :thinking:");
                    return;
                }
                String next = s.nextLine();
                if (!next.equals(headerLine)) {
                    sendMessageQueue(e, "File did not match rym export format :thinking:");
                    return;
                }
                while (s.hasNextLine()) {
                    String line = s.nextLine();
                    RYMImportRating rating = mapper.apply(line);
                    if (rating == null) {
                        sendMessageQueue(e, "File did not match rym export format :thinking:");
                        return;
                    }
                    if (rating.getRating() == 0) {
                        continue;
                    }
                    ratings.add(rating);
                }
            } catch (IOException ioException) {
                sendMessageQueue(e, "An Unexpected Error happened parsing the file :thinking:");
                return;
            }
            if (ratings.isEmpty()) {
                sendMessageQueue(e, "Rating List was empty :thinking:");
                return;
            }
            sendMessageQueue(e, String.format("Read %d ratings, now the import process will start.", ratings.size()));
            e.getChannel().sendTyping().queue();
            getService().insertRatings(e.getAuthor().getIdLong(), ratings);
            sendMessageQueue(e, String.format("Finished the import process of %s with no errors", getUserString(e, e.getAuthor().getIdLong())));
        } finally {
            usersInProcess.remove(e.getAuthor().getIdLong());
        }
    }

    @Override
    public String getUsageInstructions() {
        return getAliases().get(0) + " rym_import_file \n" + " " +
                "In order to import your data you need to actively download your rym data and then link it to the bot uploading the plain .txt file while using this command." +
                "The file can be exported and the bottom of your profile page on rym clicking on the button ***EXPORT YOUR DATA*** or ***EXPORT WITH REVIEWS***";

    }
}
