package core.parsers.params;

import core.commands.CommandUtil;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GuildParameters extends ChartParameters {
    private final boolean isGlobal;

    public GuildParameters(String[] message, MessageReceivedEvent e, int x, int y) {
        super(message, null, e.getAuthor().getIdLong(), TimeFrameEnum.ALL, x, y, e, new OptionalParameter("--notiles", 2),
                new OptionalParameter("--plays", 3),
                new OptionalParameter("--list", 4),
                new OptionalParameter("--pie", 5),
                new OptionalParameter("--global", 6));
        isGlobal = hasOptional("--global");
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    @Override
    public EmbedBuilder initEmbed(String titleInit, EmbedBuilder embedBuilder, String footerText) {
        String init;
        String thumbnail;
        if (isGlobal) {
            SelfUser selfUser = getE().getJDA().getSelfUser();
            init = selfUser.getName();
            thumbnail = selfUser.getAvatarUrl();
        } else {
            Guild guild = getE().getGuild();
            init = guild.getName();
            thumbnail = guild.getIconUrl();
        }
        return embedBuilder.setTitle(CommandUtil.cleanMarkdownCharacter(init) + titleInit + this.getTimeFrameEnum().getDisplayString())
                .setThumbnail(thumbnail)
                .setFooter(init + footerText + this.getTimeFrameEnum().getDisplayString());
    }

    @Override
    public boolean isWritePlays() {
        return !super.isWritePlays();
    }
}

