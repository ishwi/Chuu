//import core.commands.*;
//import core.commands.utils.EvalContext;
//import core.exceptions.LastFmException;
//import dao.*;
//import dao.exceptions.InstanceNotFoundException;
//import net.dv8tion.jda.api.Permission;
//import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
//
//import java.util.EnumSet;
//
//
//public class EvalTest2 {
//    public void name(EvalContext ctx) throws LastFmException, InstanceNotFoundException {
//
//        class WeezerCommand extends core.commands.InviteCommand {
//            public WeezerCommand(ChuuService dao) {
//                super(dao);
//            }
//
//
//            @Override
//            public CommandCategory getCategory() {
//                return CommandCategory.SERVER_STATS;
//            }
//
//            @Override
//            public core.parsers.Parser<core.parsers.params.CommandParameters> getParser() {
//                return new core.parsers.NoOpParser();
//            }
//
//            @Override
//            public String getDescription() {
//                return "weezer";
//            }
//
//            @Override
//            public java.util.List<String> getAliases() {
//                return java.util.List.of("weezer");
//            }
//
//            @Override
//            public String getName() {
//                return "weezer";
//            }
//            @Override
//            void onCommand(MessageReceivedEvent e) throws core.exceptions.LastFmException, core.exceptions.InstanceNotFoundException {
//                e.getChannel().sendMessage("e").queue();
//            }
//
//            @Override
//            public void onMessageReceived(MessageReceivedEvent e) {
//                e.getChannel().sendTyping().queue();
//                if (!e.isFromGuild()) {
//                    e.getChannel().sendMessage("This command only works in a server").queue();
//                    return;
//                }
//                if (e.getMessage().getContentRaw().substring(1).toLowerCase().startsWith("weezer - weezer"))
//                e.getChannel().sendMessage("https://cdn.discordapp.com/attachments/622599079918043160/709526987470930020/IMG_E0806.JPG").queue();
////                measureTime(e);
//            }
//        }
//
//
//
//        ctx.jda.addEventListener(new WeezerCommand(ctx.db));
//
//    }
//}
