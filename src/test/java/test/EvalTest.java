package test;

import core.Chuu;
import core.commands.utils.EvalContext;

import java.util.*;


import net.dv8tion.jda.api.entities.Guild;
import;
import org.junit.Test;

public class EvalTest {


    @Test
    public void name(EvalContext ctx) {
        net.dv8tion.jda.api.entities.Guild guildById = core.Chuu.getShardManager().getGuildById(476779889102684160L);
        ctx.sendMessage(guildById == null ? null : guildById.getName());
        if (guildById != null) {
            net.dv8tion.jda.api.entities.TextChannel textChannelById = guildById.getTextChannelById(566365524041269249L);
            ctx.sendMessage(textChannelById == null ? "NULLO" : textChannelById.getName());
            if (textChannelById != null) {

                textChannelById.sendMessage("<@318366600979808257> likes cucumber").mentionUsers(user.getId()).queue();
                        .allowedMentions(EnumSet.noneOf(net.dv8tion.jda.api.entities.Message.MentionType.class)).queue();
            }
        }
    }
}
