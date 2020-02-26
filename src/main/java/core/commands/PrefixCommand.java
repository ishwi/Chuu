package core.commands;

import core.Chuu;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.PrefixParser;
import dao.ChuuService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PrefixCommand extends ConcurrentCommand {
    public PrefixCommand(ChuuService dao) {
        super(dao);
        respondInPrivate = false;
        parser = new PrefixParser();
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
	protected void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        String[] parsed = parser.parse(e);
        if (parsed == null)
            return;
        Character newPrefix = parsed[0].charAt(0);
        getService().addGuildPrefix(e.getGuild().getIdLong(), newPrefix);
        Chuu.addGuildPrefix(e.getGuild().getIdLong(), newPrefix);

        sendMessageQueue(e, newPrefix.toString() + " is the new server prefix");
    }

	@Override
	public String getName() {
		return "Prefix setter";
	}

	public void onStartup(JDA jda) {

		Map<Long, Character> prefixMap = Chuu.getPrefixMap();
		List<Guild> guilds = jda.getGuilds();
		for (Guild guild : guilds) {
			long guildId = guild.getIdLong();
			if (!prefixMap.containsKey(guildId)) {
                getService().addGuildPrefix(guildId, Chuu.DEFAULT_PREFIX);
                prefixMap.put(guildId, Chuu.DEFAULT_PREFIX);
            }
		}
	}
}
