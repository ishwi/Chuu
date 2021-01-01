package core.parsers;

import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.params.CommandParameters;
import dao.exceptions.InstanceNotFoundException;
import javacutils.Pair;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static <Y> Pair<String[], Y> filterMessage(String[] ogMessage, Predicate<String> filter, Function<String, Y> mappingFuntion, Y defualt) {
        Stream<String> secondStream = Arrays.stream(ogMessage).filter(filter);
        Y apply = defualt;
        Optional<String> opt2 = secondStream.findAny();
        if (opt2.isPresent()) {
            apply = mappingFuntion.apply(opt2.get());
            ogMessage = Arrays.stream(ogMessage).filter(s -> !s.equals(opt2.get())).toArray(String[]::new);
        }
        return Pair.of(ogMessage, apply);

    }

    public T parse(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] subMessage = getSubMessage(e.getMessage());
        List<String> subMessageBuilding = new ArrayList<>();
        List<String> optionals = new ArrayList<>();

        for (String s : subMessage) {
            //eghh asdhi
            if (OptionalEntity.isWordAValidOptional(opts, s)) {
                optionals.add(OptionalEntity.getOptPartFromValid(s));
            } else {
                subMessageBuilding.add(s);
            }
        }

        Set<OptionalEntity> defaults = opts.stream().filter(OptionalEntity::isEnabledByDefault).collect(Collectors.toSet());
        for (
                OptionalEntity aDefault : defaults) {
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

    public String[] getSubMessage(Message message) {
        return getSubMessage(message.getContentRaw());

    }

    private String[] getSubMessage(String string) {
        String[] parts = string.substring(1).split("\\s+");
        return Arrays.copyOfRange(parts, 1, parts.length);

    }

    public boolean hasOptional(String optional, MessageReceivedEvent e) {
        String[] subMessage = getSubMessage(e.getMessage());
        List<String> arrayList = Arrays.asList(subMessage);
        return arrayList.stream().anyMatch(x -> OptionalEntity.isWordAValidOptional(opts, x) && opts.contains(new OptionalEntity(optional, null)));
    }

    public String getErrorMessage(int code) {
        return errorMessages.get(code);
    }


    private void sendMessage(Message message, MessageReceivedEvent e) {
        e.getChannel().sendMessage(message).reference(e.getMessage()).queue();
    }

    public void sendError(String message, MessageReceivedEvent e) {
        String errorBase = "Error on " + CommandUtil.cleanMarkdownCharacter(e.getAuthor().getName()) + "'s request:\n";
        sendMessage(new MessageBuilder().append(errorBase).append(message).build(), e);
    }

    public void sendFocusedError(String message, MessageReceivedEvent e, long discordID) {
        String username = CommandUtil.getUserInfoNotStripped(e, discordID).getUsername();
        String errorBase = "Error on " + CommandUtil.cleanMarkdownCharacter(username) + "'s request:\n";
        sendMessage(new MessageBuilder().append(errorBase).append(message).build(), e);
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
