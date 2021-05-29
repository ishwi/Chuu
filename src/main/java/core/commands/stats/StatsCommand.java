package core.commands.stats;

import core.Chuu;
import core.apis.last.entities.chartentities.ChartUtil;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.Parser;
import core.parsers.StatsParser;
import core.parsers.params.StatsParams;
import core.parsers.utils.CustomTimeFrame;
import core.services.UserInfoService;
import core.util.stats.Stats;
import dao.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.LastFMData;
import dao.entities.UserInfo;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class StatsCommand extends ConcurrentCommand<StatsParams> {
    public StatsCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<StatsParams> initParser() {
        return new StatsParser(db);
    }

    @Override
    public String getDescription() {
        return "Several stats grouped";
    }

    @Override
    public List<String> getAliases() {
        return List.of("stats", "overview", "o");
    }

    @Override
    public String getName() {
        return "Stats";
    }

    @Override
    protected void onCommand(Context e, @NotNull StatsParams params) throws LastFmException, InstanceNotFoundException {

        Set<StatsParser.StatsParam> enums = params.getEnums();
        HashMap<Stats, Integer> aux = enums.stream().collect(HashMap::new, (a, b) -> a.put(b.mode(), b.param()), HashMap::putAll);
        EnumSet<Stats> enumSet = aux.keySet().stream().collect(Collectors.toCollection(() -> EnumSet.noneOf(Stats.class)));

        if (params.isHelp()) {
            EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e);
            String title;
            if (params.isNoMatch()) {
                title = "Didn't find any statistic with that name!";
                enumSet = EnumSet.allOf(Stats.class);
            } else {
                title = "Statistic explanation";
            }
            embedBuilder
                    .setAuthor(title, PrivacyUtils.getLastFmUser(Chuu.DEFAULT_LASTFM_ID), e.getJDA().getSelfUser().getAvatarUrl())
                    .setFooter("Do " + e.getPrefix() + getAliases().get(0) + " help |statistic| for information about a certain stat!");

            var lines = enumSet.stream().sorted(Comparator.comparingInt(Enum::ordinal)).map(x -> {
                String aliases = String.join("; ", x.getAliases());
                if (!aliases.isBlank()) {
                    aliases = " (" + aliases + ")";
                }
                return "**%s** \u279C %s%s%n".formatted(x.toString(), x.getHelpMessage(), aliases);
            }).toList();

            StringBuilder str = new StringBuilder();
            for (int i = 0; i < 20 && i < lines.size(); i++) {
                str.append(lines.get(i));
            }
            e.sendMessage(embedBuilder.setDescription(str).build()).queue(z -> new Reactionary<>(lines, z, 20, embedBuilder, false));
            return;
        }

        LastFMData data = params.getUser();

        CustomTimeFrame tfe = params.getCustomTimeFrame();

        UserInfo userInfo;
        if (enumSet.contains(Stats.ALL) || enumSet.isEmpty()) {
            enumSet = EnumSet.complementOf(EnumSet.of(Stats.ALL));
            enumSet.removeIf(Stats::hideInAll);
            if (!tfe.isAllTime()) {
                enumSet.remove(Stats.JOINED);
            }
            userInfo = new UserInfoService(db).refreshUserInfo(data);
        } else {
            userInfo = new UserInfoService(db).maybeRefresh(data);
        }

        int totalPlays = userInfo.getPlayCount();
        int timestamp = userInfo.getUnixtimestamp();
        if (!tfe.isAllTime()) {
            Long from = ChartUtil.getFromTo(tfe).getLeft();
            totalPlays = lastFM.getInfoPeriod(data, from);
        }
        for (Stats stats : enumSet) {
            Integer integer = aux.get((stats));
            if (integer == null) {
                aux.put(stats, params.getGlobalParam());
            }
        }

        Stats.StatsResult result = Stats.process(data, db, lastFM, enumSet, userInfo, totalPlays, timestamp, tfe, aux, params.getNp());
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, e.getAuthor().getIdLong());
        EmbedBuilder eb = new ChuuEmbedBuilder(e)
                .setAuthor(uInfo.getUsername(), PrivacyUtils.getLastFmUser(data.getName()), userInfo.getImage())
                .setDescription(result.description())
                .setFooter(result.footer());

        if (enumSet.contains(Stats.ALL)) {
            eb.setThumbnail(userInfo.getImage());
        }
        e.sendMessage(eb
                .build()
        ).queue();

    }
}
