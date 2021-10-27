package core.commands.moderation;

import com.github.natanbc.javaeval.CompilationException;
import com.github.natanbc.javaeval.CompilationResult;
import com.github.natanbc.javaeval.JavaEvaluator;
import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.EvalClassLoader;
import core.commands.utils.EvalContext;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ServiceView;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.Nonnull;
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
            import core.Chuu.*;
            import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
            """;
    private static final Pattern emmbed = Pattern.compile("```(?:java)?[\\s\\S]*```");
    private static Long ownerId = null;

    public EvalCommand(ServiceView dao) {

        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
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
    public void onCommand(Context e, @Nonnull CommandParameters params) {
        if (ownerId == null) {
            e.getJDA().retrieveApplicationInfo().queue(x -> ownerId = x.getOwner().getIdLong());
            return;
        }
        if (ownerId != e.getAuthor().getIdLong()) {
            return;
        }
        String contentRaw = e instanceof ContextMessageReceived mes ? mes.e().getMessage().getContentRaw() : "";
        if (contentRaw.length() < 5)
            return;
        contentRaw = contentRaw.substring(5).trim();
        if ((emmbed.matcher(contentRaw).matches())) {
            contentRaw = contentRaw.replaceAll("```(java)?", "");
        }
        contentRaw = contentRaw.strip();
        String imports = Arrays.stream(contentRaw.split("\n")).takeWhile(it -> it.startsWith("import ")).collect(Collectors.joining("\n"));
        String code = Arrays.stream(contentRaw.split("\n")).dropWhile(it -> it.startsWith("import ")).collect(Collectors.joining("\n"));


        EvalContext evalContext = new EvalContext(e.getJDA(), e, e.getAuthor(), e.isFromGuild() ? e.getGuild() : null, null, db, lastFM);
        String source = JAVA_EVAL_IMPORTS + "\n" + imports + "\n" +
                "public class Eval {\n" +
                "   public static Object run(EvalContext ctx) throws Throwable {\n" +
                "       try {\n" +
                "           return null;\n" +
                "       } finally {\n" +
                "           " + (code + ";").replaceAll(";{2,}", ";") + "\n" +
                "       }\n" +
                "   }\n" +
                "}";
        try {
            JavaEvaluator javaEvaluator = new JavaEvaluator();
            CompilationResult r = javaEvaluator.compile()
                    .addCompilerOptions("-Xlint:unchecked", "--target=17", "--enable-preview", "--source=17")
                    .source("Eval", source
                    )
                    .execute();
            EvalClassLoader ecl = new EvalClassLoader();
            r.classes().forEach((name, bytes) -> ecl.define(bytes));

            ecl.loadClass("core.commands.Eval").getMethod("run", EvalContext.class).invoke(null, evalContext);
        } catch (CompilationException noSuchMethodException) {
            EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e).addField("Code", "```java\n" + StringUtils.abbreviate(source, 1000) + "\n```\n", false);
            noSuchMethodException.getDiagnostics().forEach(z -> embedBuilder.addField(String.valueOf(z.getLineNumber()), StringUtils.abbreviate(z.toString(), 1000), false));
            e.sendMessage(embedBuilder.build()).queue();
        } catch (Exception ex2) {
            e.sendMessage(new ChuuEmbedBuilder(e).setDescription("```\n" + StringUtils.abbreviate(ExceptionUtils.getStackTrace(ExceptionUtils.getRootCause(ex2)), 2000) + "```").addField("Code", "```java\n" + StringUtils.abbreviate(source, 1000) + "\n```\n", false).build()).queue();
        }

    }


}
