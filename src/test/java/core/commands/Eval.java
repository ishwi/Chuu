package core.commands;

import core.commands.utils.EvalContext;
import dao.ChuuService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Collections;
import java.util.List;

@State(Scope.Benchmark)
public class Eval {

    @Param({"10"})
    public int iterations;

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    static BufferedImage deepCopy(BufferedImage bi) {
        EvalContext ctx = null;


        class A extends core.commands.abstracts.ConcurrentCommand<core.parsers.params.CommandParameters> {
            public A(ChuuService dao) {
                super(dao);
            }

            @Override
            protected core.commands.utils.CommandCategory initCategory() {
                return core.commands.utils.CommandCategory.BOT_INFO;
            }

            @Override
            public core.parsers.Parser<core.parsers.params.CommandParameters> initParser() {
                return new core.parsers.NoOpParser();
            }

            @Override
            public String getDescription() {
                return "Why";
            }

            @Override
            public List<String> getAliases() {
                return Collections.singletonList("why");
            }

            @Override
            public String getName() {
                return "why";
            }

            @Override
            protected void onCommand(MessageReceivedEvent e, core.parsers.params.CommandParameters params) {
                sendMessageQueue(e, "<https://support.last.fm/t/user-gettopalbums-not-returning-album-artwork-with-high-values-of-limit>");
            }
        }
        core.Chuu.getShardManager().getGuildById(682909819702738967L).getJDA().addEventListener(new A(core.Chuu.getDao()));


        core.Chuu.getShardManager().getGuildCache().stream().mapToLong(t -> t.getMembers().size()).sum();
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public void measureName() {


    }
}
