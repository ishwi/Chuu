package core.parsers;

import core.commands.CommandUtil;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.CommandParameters;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Parser<T extends CommandParameters> {
    final Map<Integer, String> errorMessages = new HashMap<>(10);
    final Set<OptionalEntity> opts = new HashSet<>();


    Parser() {
        setUpErrorMessages();
        setUpOptionals();
    }

    Parser(OptionalEntity... opts) {
        this();
        this.opts.addAll(Arrays.asList(opts));
    }


    void setUpOptionals() {
        //Do nothing
    }

    protected abstract void setUpErrorMessages();


    public T parse(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
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

        Set<OptionalEntity> defaults = opts.stream().filter(OptionalEntity::isEnabledByDefault).collect(Collectors.toSet());
        for (OptionalEntity aDefault : defaults) {
            if (!optionals.contains(aDefault.getBlockedBy())) {
                optionals.add(aDefault.getValue());
            }
        }

        T preParams = parseLogic(e, subMessageBuilding.toArray(new String[0]));
        if (preParams != null) {
            preParams.initParams(optionals);
        }
        return preParams;
    }

    protected abstract T parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException, LastFmException;

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


    private void sendMessage(Message message, MessageReceivedEvent e) {
        e.getChannel().sendMessage(message).queue();
    }

    public void sendError(String message, MessageReceivedEvent e) {
        String errorBase = "Error on " + CommandUtil.cleanMarkdownCharacter(e.getAuthor().getName()) + "'s request:\n";
        sendMessage(new MessageBuilder().append(CommandUtil.sanitizeUserString(errorBase)).append(CommandUtil.sanitizeUserString(message)).build(), e);
    }

    public void sendFocusedError(String message, MessageReceivedEvent e, long discordID) {
        String username = CommandUtil.getUserInfoNotStripped(e, discordID).getUsername();
        String errorBase = "Error on " + CommandUtil.cleanMarkdownCharacter(username) + "'s request:\n";
        sendMessage(new MessageBuilder().append(CommandUtil.sanitizeUserString(errorBase)).append(message).build(), e);
    }


    public String getUsage(String commandName) {
        StringBuilder s = new StringBuilder();
        for (OptionalEntity opt : opts) {
            if (!opt.isEnabledByDefault()) {
                s.append(opt.getDefinition());
            }
        }
        return getUsageLogic(commandName) + s;

    }

    public void replaceOptional(String previousOptional, OptionalEntity optionalEntity) {
        opts.remove(new OptionalEntity(previousOptional, null));
        opts.add(optionalEntity);
    }

    public void addOptional(OptionalEntity... optionalEntity) {
        this.opts.addAll(Arrays.asList(optionalEntity));
    }

    public void removeOptional(String previousOptional) {
        opts.remove(new OptionalEntity(previousOptional, null));
    }

    public abstract String getUsageLogic(String commandName);


}
