package core.parsers;

import core.parsers.params.ArtistUrlParameters;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ArtistUrlParser extends Parser<ArtistUrlParameters> {
    @Override
    public void setUpErrorMessages() {
        errorMessages.put(0, "You need to specify the artist and the url!!");
        errorMessages.put(1, "You didn't specify a valid URL");
        errorMessages.put(2, "Couldn't get an Image from link supplied");


    }

    @Override
    public ArtistUrlParameters parseLogic(MessageReceivedEvent e, String[] subMessage) {

        boolean noUrl = true;

        String artist;
        String url = null;
        if (subMessage.length >= 1) {
            StringBuilder a = new StringBuilder();
            for (String s : subMessage) {
                if (noUrl && UrlParser.isValidURL(s)) {
                    noUrl = false;
                    url = s;
                    continue;
                }
                a.append(s).append(" ");
            }
            artist = a.toString().trim();

        } else {
            sendError(getErrorMessage(0), e);
            return null;
        }
        if (url == null) {
            if (e.getMessage().getAttachments().isEmpty()) {
                sendError(getErrorMessage(1), e);
                return null;

            } else {
                Message.Attachment attachment = e.getMessage().getAttachments().get(0);
                url = attachment.getUrl();
            }
        }
        return new ArtistUrlParameters(e, artist, e.getAuthor(), url);
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *artist url***\n";

    }


}
