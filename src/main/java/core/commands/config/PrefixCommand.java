package core.commands.config;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.Parser;
import core.parsers.PrefixParser;
import core.parsers.params.CharacterParameters;
import core.services.PrefixService;
import dao.ServiceView;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PrefixCommand extends ConcurrentCommand<CharacterParameters> {
    public PrefixCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<CharacterParameters> initParser() {
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
    protected void onCommand(Context e, @Nonnull CharacterParameters params) {


        char newPrefix = params.getaChar();
        PrefixService prefixService = Chuu.prefixService;
        db.addGuildPrefix(prefixService.getPrefixMap(), e.getGuild().getIdLong(), newPrefix);
        prefixService.addGuildPrefix(e.getGuild().getIdLong(), newPrefix);

        sendMessageQueue(e, newPrefix + " is the new server prefix");
    }

    @Override
    public String getName() {
        return "Prefix setter";
    }

    public void onStartup(ShardManager jda) {

        Map<Long, Character> prefixMap = Chuu.prefixService.getPrefixMap();
        List<Guild> guilds = jda.getGuilds();
        for (Guild guild : guilds) {
            long guildId = guild.getIdLong();
            if (!prefixMap.containsKey(guildId)) {
                db.addGuildPrefix(prefixMap, guildId, Chuu.DEFAULT_PREFIX);
                prefixMap.put(guildId, Chuu.DEFAULT_PREFIX);
            }
        }
    }
}
