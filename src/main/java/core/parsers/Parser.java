package core.parsers;

import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.commands.ContextSlashReceived;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.UsageLogic;
import core.parsers.params.CommandParameters;
import dao.exceptions.InstanceNotFoundException;
import javacutils.Pair;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Parser<T extends CommandParameters> {
    final Map<Integer, String> errorMessages = new HashMap<>(10);
    final Set<OptionalEntity> opts = new LinkedHashSet<>();


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

    public static <Y> Pair<String[], Y> filterMessage(String[] ogMessage, Predicate<String> filter, Function<String, Y> mapper, Y yDefault) {
        Stream<String> secondStream = Arrays.stream(ogMessage).filter(filter);
        Y apply = yDefault;
        Optional<String> opt2 = secondStream.findAny();
        if (opt2.isPresent()) {
            apply = mapper.apply(opt2.get());
            ogMessage = Arrays.stream(ogMessage).filter(s -> !s.equals(opt2.get())).toArray(String[]::new);
        }
        return Pair.of(ogMessage, apply);

    }

    public T parseMessage(Context e) throws LastFmException, InstanceNotFoundException {
        String[] subMessage = getSubMessage(e);
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

        processOpts(optionals);
        T preParams = parseLogic(e, subMessageBuilding.toArray(new String[0]));
        if (preParams != null) {
            preParams.initParams(optionals);
        }
        return preParams;
    }

    public T parse(Context e) throws LastFmException, InstanceNotFoundException {
        if (e instanceof ContextMessageReceived mes) {
            return parseMessage(mes);
        } else if (e instanceof ContextSlashReceived sce) {
            return parseSlash(sce);
        }
        return null;
    }

    public T parseSlash(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        List<SlashCommandEvent.OptionData> strings = e.getOptionsByType(OptionType.STRING);
        List<String> optionals = new ArrayList<>();
        for (SlashCommandEvent.OptionData s : strings) {
            if (s.getAsString().equals("yes") && opts.contains(new OptionalEntity(s.getName(), null))) {
                optionals.add(s.getName());
            }
        }

        processOpts(optionals);
        T preParams = parseSlashLogic(ctx);
        if (preParams != null) {
            preParams.initParams(optionals);
        }
        return preParams;
    }

    private void processOpts(List<String> optionals) {
        Set<OptionalEntity> defaults = opts.stream().filter(OptionalEntity::isEnabledByDefault).collect(Collectors.toSet());
        for (
                OptionalEntity aDefault : defaults) {
            boolean block = false;
            for (String blocked : aDefault.getBlockedBy()) {
                if (optionals.contains(blocked)) {
                    block = true;
                    break;
                }
            }
            if (!block) {
                optionals.add(aDefault.getValue());
            }
        }
    }

    public T parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        throw new UnsupportedOperationException();
    }

    protected abstract T parseLogic(Context e, String[] words) throws InstanceNotFoundException, LastFmException;

    public String[] getSubMessage(Context context) {
        if (context instanceof ContextMessageReceived mes) {
            return getSubMessage(mes.e().getMessage().getContentRaw());
        } else {

            throw new IllegalStateException();
        }

    }

    public String getAlias(Context context) {
        if (context instanceof ContextMessageReceived mes) {
            return mes.e().getMessage().getContentRaw().substring(1).toLowerCase();
        } else {
            throw new IllegalStateException();
        }

    }


    private String[] getSubMessage(String string) {
        String[] parts = string.substring(1).split("\\s+");
        return Arrays.copyOfRange(parts, 1, parts.length);

    }

    public boolean hasOptional(String optional, Context e) {
        String[] subMessage = getSubMessage(e);
        List<String> arrayList = Arrays.asList(subMessage);
        return arrayList.stream().anyMatch(x -> OptionalEntity.isWordAValidOptional(opts, x) && opts.contains(new OptionalEntity(optional, null)));
    }

    public String getErrorMessage(int code) {
        return errorMessages.get(code);
    }


    private void sendMessage(Message message, Context e) {
        if (e instanceof ContextMessageReceived mes) {
            mes.getChannel().sendMessage(message).reference(mes.e().getMessage()).queue();
        } else {
            e.sendMessage(message).queue();
        }
    }

    public void sendError(String message, Context e) {
        String errorBase = "Error on " + CommandUtil.cleanMarkdownCharacter(e.getAuthor().getName()) + "'s request:\n";
        sendMessage(new MessageBuilder().append(errorBase).append(message).build(), e);
    }


    public String getUsage(String commandName) {
        return new UsageLogic(commandName, getUsages(), opts).getUsage();
    }

    public abstract List<Explanation> getUsages();

    public List<OptionalEntity> getOptionals() {
        return new ArrayList<>(opts);
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


}
