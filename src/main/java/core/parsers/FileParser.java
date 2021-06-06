package core.parsers;

import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.params.UrlParameters;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Collections;
import java.util.List;

public class FileParser extends Parser<UrlParameters> {
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
    protected UrlParameters parseLogic(Context e, String[] words) {
        List<Message.Attachment> attachments = e instanceof ContextMessageReceived mes ? mes.e().getMessage().getAttachments() : Collections.emptyList();
        String url;
        if (attachments.isEmpty()) {
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
        int i = url.lastIndexOf('.');
        if (i == -1 || i == url.length() - 1) {
            sendError(getErrorMessage(3), e);
            return null;
        }
        String substring = url.substring(i + 1);

        if (!substring.equalsIgnoreCase(fileExtension)) {
            sendError(getErrorMessage(3), e);
            return null;
        }
        return new UrlParameters(e, url);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(() -> new ExplanationLineType("File", "File must be a " + fileExtension + "file", OptionType.STRING));
    }

}
