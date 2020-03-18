package core.parsers;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class FileParser extends Parser {
    private final String fileExtension;

    public FileParser(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    @Override
    protected void setUpErrorMessages() {
        this.errorMessages.put(1, "You must introduce a file or a link to a file");
        this.errorMessages.put(2, "File introduced was not a link to a file");
        this.errorMessages.put(3, "File introduced is not a valid file");
        this.errorMessages.put(4, "Link introduced is not a valid link");


    }

    @Override
    protected String[] parseLogic(MessageReceivedEvent e, String[] words) {
        List<Message.Attachment> attachments = e.getMessage().getAttachments();

        String url;
        if (attachments.size() == 0) {
            if (words.length < 1) {
                sendError(getErrorMessage(1), e);
                return null;
            }
            if (!UrlParser.isValidURL(words[0])) {
                sendError(getErrorMessage(4), e);
                return null;
            }
            url = words[0];
        } else {
            if (attachments.size() != 1) {
                sendError(getErrorMessage(1), e);
                return null;
            }
            Message.Attachment attachment = attachments.get(0);
            url = attachment.getUrl();
        }
        if (url == null) {
            sendError(getErrorMessage(2), e);
            return null;
        }
        int i = url.lastIndexOf(".");
        if (i == -1 || i == url.length() - 1) {
            sendError(getErrorMessage(3), e);
            return null;
        }
        String substring = url.substring(i + 1);

        if (!substring.equalsIgnoreCase(fileExtension)) {
            sendError(getErrorMessage(3), e);
            return null;
        }
        return new String[]{url};
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *file*\n\t File must be a " + fileExtension + " file\n\n";

    }
}
