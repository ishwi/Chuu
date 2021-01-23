package core.commands.config;

import core.Chuu;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.CharacterParser;
import core.parsers.Parser;
import core.parsers.PrefixParser;
import dao.ChuuService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PrefixCommand extends ConcurrentCommand<CharacterParser> {
    public PrefixCommand(ChuuService dao) {
        super(dao);
        respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<CharacterParser> initParser() {
        return new PrefixParser();
    }

    @Override
    public String getDescription() {
        return "Sets the prefix that the bot will respond to";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("prefix");
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull CharacterParser params) {


        char newPrefix = params.getaChar();
        getService().addGuildPrefix(Chuu.getPrefixMap(), e.getGuild().getIdLong(), newPrefix);
        Chuu.addGuildPrefix(e.getGuild().getIdLong(), newPrefix);

        sendMessageQueue(e, newPrefix + " is the new server prefix");
    }

    @Override
    public String getName() {
        return "Prefix setter";
    }

    public void onStartup(ShardManager jda) {

        Map<Long, Character> prefixMap = Chuu.getPrefixMap();
        List<Guild> guilds = jda.getGuilds();
        for (Guild guild : guilds) {
            long guildId = guild.getIdLong();
            if (!prefixMap.containsKey(guildId)) {
                getService().addGuildPrefix(prefixMap, guildId, Chuu.DEFAULT_PREFIX);
                prefixMap.put(guildId, Chuu.DEFAULT_PREFIX);
            }
        }
    }
}
