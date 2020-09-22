package core.commands;

import com.github.natanbc.javaeval.CompilationResult;
import com.github.natanbc.javaeval.JavaEvaluator;
import core.Chuu;
import core.commands.utils.EvalClassLoader;
import core.commands.utils.EvalContext;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EvalCommand extends ConcurrentCommand<CommandParameters> {
    private static Long ownerId;
    private static final String JAVA_EVAL_IMPORTS = "" +
            "package core.commands;\n " +
            "import dao.ChuuService;\n" +
            "import core.commands.utils.EvalContext;\n" +
            "import core.apis.last.ConcurrentLastFM;\n" +
            "import dao.entities.*;\n" +
            "import java.util.*;\n" +
            "import net.dv8tion.jda.api.JDA;\n" +
            "import net.dv8tion.jda.api.entities.Guild;\n" +
            "import net.dv8tion.jda.api.entities.User;\n" +
            "import net.dv8tion.jda.api.events.message.MessageReceivedEvent;\n";
    private static final Pattern emmbed = Pattern.compile("```(:?java)?[\\s\\S]*```");

    public EvalCommand(ChuuService dao) {

        super(dao);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<CommandParameters> getParser() {
        return new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "Evaluate java code";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("eval");
    }

    @Override
    public String getName() {
        return "Eval";
    }

    @Override
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        if (ownerId != e.getAuthor().getIdLong()) {
            return;
        }
        String contentRaw = e.getMessage().getContentRaw();
        if (contentRaw.length() < 5)
            return;
        contentRaw = contentRaw.substring(5).trim();

        if ((emmbed.matcher(contentRaw).matches())) {
            contentRaw = contentRaw.replaceAll("```(java)?", "");
        }
        EvalContext evalContext = new EvalContext(e.getJDA(), e, e.getAuthor(), e.isFromGuild() ? e.getGuild() : null, null, getService(), lastFM);
        try {
            JavaEvaluator javaEvaluator = new JavaEvaluator();
            CompilationResult r = javaEvaluator.compile()
                    .addCompilerOptions("-Xlint:unchecked")
                    .source("Eval", JAVA_EVAL_IMPORTS + "\n\n" +
                            "public class Eval {\n" +
                            "   public static Object run(EvalContext ctx) throws Throwable {\n" +
                            "       try {\n" +
                            "           return null;\n" +
                            "       } finally {\n" +
                            "           " + (contentRaw + ";").replaceAll(";{2,}", ";") + "\n" +
                            "       }\n" +
                            "   }\n" +
                            "}"
                    )
                    .execute();

            EvalClassLoader ecl = new EvalClassLoader();
            r.getClasses().forEach((name, bytes) -> ecl.define(bytes));
            var a = core.Chuu.getPrefixMap();

            ecl.loadClass("core.commands.Eval").getMethod("run", EvalContext.class).invoke(null, evalContext);
        } catch (Exception noSuchMethodException) {
            sendMessageQueue(e, "```\n" + noSuchMethodException.getCause() + "```");
            noSuchMethodException.printStackTrace();
        }

    }

    public void setOwnerId(ShardManager shard) {
        shard.getShards().get(0).retrieveApplicationInfo().queue(x -> ownerId = x.getOwner().getIdLong());
    }
}
