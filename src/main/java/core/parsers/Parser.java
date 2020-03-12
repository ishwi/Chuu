package core.parsers;

import core.commands.CommandUtil;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;

public abstract class Parser {
    final Map<Integer, String> errorMessages = new HashMap<>(10);
    final List<OptionalEntity> opts = new ArrayList<>();


    Parser() {
        setUpErrorMessages();
        setUpOptionals();
    }

    void setUpOptionals() {
        //Do nothing
    }

    protected abstract void setUpErrorMessages();


    public String[] parse(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] subMessage = getSubMessage(e.getMessage());
        List<String> subMessageBuilding = new ArrayList<>();
        List<String> optionals = new ArrayList<>();

        for (String s : subMessage) {
            //eghh asdhi
            OptionalEntity optionalEntity = new OptionalEntity(s, "");
            if (opts.contains(optionalEntity)) {
                optionals.add(optionalEntity.getValue());
            } else {
                subMessageBuilding.add(s);
            }
        }

        String[] preOptionaledMessage = parseLogic(e, subMessageBuilding.toArray(new String[0]));
        if (preOptionaledMessage == null)
            return null;
        String[] withFlags = Arrays.copyOf(preOptionaledMessage, opts.size() + preOptionaledMessage.length);
        int counter = preOptionaledMessage.length;
        for (OptionalEntity opt : opts) {
            withFlags[counter++] = String.valueOf(optionals.contains(opt.getValue()));
        }
        return withFlags;
    }

    protected abstract String[] parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException;

    String[] getSubMessage(Message message) {
        return getSubMessage(message.getContentRaw());

    }

    private String[] getSubMessage(String string) {
        String[] parts = string.substring(1).split("\\s+");
        return Arrays.copyOfRange(parts, 1, parts.length);

    }

    public String getErrorMessage(int code) {
        return errorMessages.get(code);
    }


    String artistMultipleWords(String[] message) {
        String artist;
        if (message.length > 1) {
            StringBuilder a = new StringBuilder();
            for (String s : message) {
                a.append(s).append(" ");
            }
            artist = a.toString().trim();
        } else {
            artist = message[0];
        }
        return artist;
    }


    private void sendMessage(Message message, MessageReceivedEvent e) {
        e.getChannel().sendMessage(message).queue();
    }

    public void sendError(String message, MessageReceivedEvent e) {
        String errorBase = "Error on " + e.getAuthor().getName() + "'s request:\n";
        sendMessage(new MessageBuilder().append(CommandUtil.sanitizeUserString(errorBase)).append(CommandUtil.sanitizeUserString(message)).build(), e);
    }


    public String getUsage(String commandName) {
        StringBuilder s = new StringBuilder();
        for (OptionalEntity opt : opts) {
            s.append(opt.getDefinition());
        }
        return getUsageLogic(commandName) + s;

    }

    public void addOptional(OptionalEntity... optionalEntity) {
        this.opts.addAll(Arrays.asList(optionalEntity));
    }

    public abstract String getUsageLogic(String commandName);


}
