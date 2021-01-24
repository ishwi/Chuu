package core.commands.moderation;

import com.github.natanbc.javaeval.CompilationResult;
import com.github.natanbc.javaeval.JavaEvaluator;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.EvalClassLoader;
import core.commands.utils.EvalContext;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EvalCommand extends ConcurrentCommand<CommandParameters> {
    private static final String JAVA_EVAL_IMPORTS = """
            package core.commands;
             import dao.ChuuService;
            import core.commands.utils.EvalContext;
            import core.apis.last.ConcurrentLastFM;
            import dao.entities.*;
            import java.util.*;
            import net.dv8tion.jda.api.JDA;
            import net.dv8tion.jda.api.entities.Guild;
            import net.dv8tion.jda.api.entities.User;
            import core.Chuu.*;import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
            """;
    private static final Pattern emmbed = Pattern.compile("```(:?java)?[\\s\\S]*```");
    private static Long ownerId = null;

    public EvalCommand(ChuuService dao) {

        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<CommandParameters> initParser() {
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
    protected void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) {
        if (ownerId == null) {
            e.getJDA().retrieveApplicationInfo().queue(x -> ownerId = x.getOwner().getIdLong());
            return;
        }
        if (ownerId != e.getAuthor().getIdLong()) {
            return;
        }
        String contentRaw = e.getMessage().getContentRaw();
        if (contentRaw.length() < 5)
            return;
        contentRaw = contentRaw.substring(5).trim();
        String imports = Arrays.stream(contentRaw.split("\n")).takeWhile(it -> it.startsWith("import ")).collect(Collectors.joining("\n"));
        String code = Arrays.stream(contentRaw.split("\n")).dropWhile(it -> it.startsWith("import ")).collect(Collectors.joining("\n"));


        if ((emmbed.matcher(contentRaw).matches())) {
            contentRaw = contentRaw.replaceAll("```(java)?", "");
        }
        EvalContext evalContext = new EvalContext(e.getJDA(), e, e.getAuthor(), e.isFromGuild() ? e.getGuild() : null, null, db, lastFM);
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
