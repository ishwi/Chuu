package core.commands.rym;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.Parser;
import core.parsers.UrlParser;
import core.parsers.params.UrlParameters;
import core.util.ServiceView;
import dao.entities.RYMImportRating;
import dao.exceptions.InstanceNotFoundException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RYMDumpImportCommand extends ConcurrentCommand<UrlParameters> {
    private static final List<String> headerLine = List.of("RYM Album", "First Name", "Last Name", "First Name localized", "Last Name localized", "Title", "Release_Date", "Rating", "Ownership", "Purchase Date", "Media Type", "Review");
    private static final Pattern unlocalized = Pattern.compile("(.*) \\[(.*)] ?");
    private static final Set<Long> usersInProcess = new HashSet<>();
    private static final Function<CSVRecord, RYMImportRating> mapper = (line) -> {
        if (line.size() != 12) {
            return null;
        }
        try {
            long rymId = Long.parseLong(line.get(0));
            String firstName = line.get(1);
            String lastName = line.get(2);
            String firstNameLocalized = line.get(3);
            String lastNameLocalized = line.get(4);
            String title = line.get(5);
            Year year = line.get(6).isEmpty() ? null : Year.parse(line.get(6));
            Byte rating = Byte.valueOf(line.get(7));
            boolean ownership = line.get(8).equals("Y");
            Year purchaseDate = line.get(9).isEmpty() ? null : Year.parse(line.get(9));
            String mediaType = line.get(10);
            String review = line.get(11);
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
    private final ReentrantLock reentrantLock = new ReentrantLock();

    public RYMDumpImportCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.RYM;
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
    public void onCommand(Context e, @Nonnull UrlParameters params) {


        List<RYMImportRating> ratings = new ArrayList<>();
        String url = params.getUrl();
        if (url.isBlank()) {
            sendMessageQueue(e, "You need to upload a file :thinking:");
            return;
        }
        try {
            db.findLastFMData(e.getAuthor().getIdLong());
        } catch (InstanceNotFoundException instanceNotFoundException) {
            parser.sendError("You need to have a linked last.fm account in order to import your ratings :(", e);
            return;
        }
        Pattern compile = Pattern.compile("https?://rateyourmusic.com/");
        if (compile.matcher(url).matches()) {
            sendMessageQueue(e, "You can't link directly to the export page, you should download the file and upload it to discord");
            return;
        }
        try {
            try {
                reentrantLock.lock();
                try {
                    if (usersInProcess.contains(e.getAuthor().getIdLong())) {
                        sendMessageQueue(e, "Your previous import command is still being processed");
                        return;
                    }
                    usersInProcess.add(e.getAuthor().getIdLong());
                } finally {
                    reentrantLock.unlock();
                }

                URL url1 = new URL(url);
                int i = 0;
                try (InputStream in = url1.openStream();
                     InputStreamReader reader = new InputStreamReader(in);
                     CSVParser parse = CSVFormat.Builder.create().setSkipHeaderRecord(false).build().parse(reader)) {
                    for (CSVRecord record : parse) {
                        if (i++ == 0) {
                            if (!record.stream().map(String::trim).toList().equals(headerLine)) {
                                sendMessageQueue(e, "Header row for the csv didn't match the expected format :thinking:");
                                return;
                            }
                        } else {
                            RYMImportRating rating = mapper.apply(record);
                            if (rating == null) {
                                sendMessageQueue(e, "Following line made the import process crash: " + record.stream().collect(Collectors.joining(",")));
                                return;
                            }
                            if (rating.getRating() == 0) {
                                continue;
                            }
                            ratings.add(rating);
                        }
                    }
                }
                if (i == 0) {
                    sendMessageQueue(e, "File did not match rym export format :thinking:");
                    return;
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
            db.insertRatings(e.getAuthor().getIdLong(), ratings);
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
