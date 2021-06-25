package core.parsers;

import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.commands.ContextSlashReceived;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.UsageLogic;
import core.parsers.params.CommandParameters;
import core.parsers.utils.OptionalEntity;
import dao.exceptions.InstanceNotFoundException;
import javacutils.Pair;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Parser<T extends CommandParameters> {
    final Map<Integer, String> errorMessages = new HashMap<>(10);
    final Set<OptionalEntity> opts = new LinkedHashSet<>();
    final Map<String, OptionalEntity> optAliases = new HashMap<>();


    Parser() {
        setUpErrorMessages();
        setUpOptionals();
    }

    Parser(OptionalEntity... opts) {
        this();
        for (OptionalEntity opt : opts) {
            addOptional(opt);
        }
    }

    public static <Y> Pair<String[], Y> filterMessage(String[] ogMessage, Predicate<String> filter, Function<String, Y> mapper, Y yDefault) {
        Stream<String> secondStream = Arrays.stream(ogMessage).filter(filter);
        Y apply = yDefault;
        Optional<String> opt2 = secondStream.findAny();
        if (opt2.isPresent()) {
            apply = mapper.apply(opt2.get());
            AtomicBoolean atomicBoolean = new AtomicBoolean(false);
            ogMessage = Arrays.stream(ogMessage).filter(s -> !s.equals(opt2.get()) || !atomicBoolean.compareAndSet(false, true)).toArray(String[]::new);
        }
        return Pair.of(ogMessage, apply);

    }

    private void addOptional(OptionalEntity opt) {
        opts.add(opt);
        opt.aliases().forEach(z -> optAliases.put(z, opt));
    }

    void setUpOptionals() {
        //Do nothing
    }

    protected abstract void setUpErrorMessages();

    public final T parseMessage(Context e) throws LastFmException, InstanceNotFoundException {
        String[] subMessage = getSubMessage(e);
        List<String> subMessageBuilding = new ArrayList<>();
        List<String> optionals = new ArrayList<>();
        for (String s : subMessage) {
            //eghh asdhi
            if (OptionalEntity.isWordAValidOptional(opts, optAliases, s)) {
                optionals.add(OptionalEntity.getOptPartFromValid(s, opts, optAliases));
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

    public final T parseSlash(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        List<OptionMapping> strings = e.getOptionsByType(OptionType.STRING);
        List<String> optionals = new ArrayList<>();
        for (OptionMapping s : strings) {
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
            for (String blocked : aDefault.blockedBy()) {
                if (optionals.contains(blocked)) {
                    block = true;
                    break;
                }
            }
            if (!block) {
                optionals.add(aDefault.value());
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
        return arrayList.stream().anyMatch(x -> OptionalEntity.isWordAValidOptional(opts, optAliases, x) && (opts.contains(new OptionalEntity(optional, null)) || optAliases.containsKey(optional)));
    }

    public String getErrorMessage(int code) {
        return errorMessages.get(code);
    }


    public void sendError(String message, Context e) {
        String errorBase = "Error on " + CommandUtil.escapeMarkdown(e.getAuthor().getName()) + "'s request:\n";
        e.sendMessage(errorBase + message).queue();
    }


    public String getUsage(String commandName) {
        return new UsageLogic(commandName, getUsages(), opts).getUsage();
    }

    public abstract List<Explanation> getUsages();

    public List<OptionalEntity> getOptionals() {
        return new ArrayList<>(opts);
    }


    public Parser<T> replaceOptional(String previousOptional, OptionalEntity optionalEntity) {
        removeOptional(previousOptional);
        addOptional(optionalEntity);
        return this;
    }

    public Parser<T> addOptional(OptionalEntity... optionalEntity) {
        for (OptionalEntity entity : optionalEntity) {
            addOptional(entity);
        }
        return this;
    }

    public void removeOptional(String previousOptional) {
        Optional<OptionalEntity> opts = this.opts.stream().filter(w -> w.equals(new OptionalEntity(previousOptional, null))).findAny();
        opts.ifPresent(w -> {
            this.opts.remove(w);
            w.aliases().forEach(this.optAliases::remove);
        });
    }


}
