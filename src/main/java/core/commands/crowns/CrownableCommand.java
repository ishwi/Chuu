package core.commands.crowns;

import core.commands.Context;
import core.commands.abstracts.ListCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.otherlisteners.Reactionary;
import core.parsers.NumberParser;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import core.services.ColorService;
import dao.ChuuService;
import dao.entities.CrownableArtist;
import dao.entities.DiscordUserDisplay;
import dao.utils.LinkUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class CrownableCommand extends ListCommand<CrownableArtist, NumberParameters<ChuuDataParams>> {

    public CrownableCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> initParser() {

        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can set a distance to show crowns achievable within it";
        OnlyUsernameParser onlyUsernameParser = new OnlyUsernameParser(db);
        onlyUsernameParser.addOptional(new OptionalEntity("nofirst", "show only the artists in which you are not first"));
        onlyUsernameParser.addOptional(new OptionalEntity("server", "make the ranking only count for this server"));
        onlyUsernameParser.addOptional(new OptionalEntity("secondonly", "only shows artist where you are second"));
        onlyUsernameParser.addOptional(new OptionalEntity("second", "do the same as --secondonly"));
        onlyUsernameParser.addOptional(new OptionalEntity("onlysecond", "do the same as --secondonly"));
        onlyUsernameParser.addOptional(new OptionalEntity("server", "make the ranking only count for this server"));

        return new NumberParser<>(onlyUsernameParser,
                (long) Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                map, s, false, true, false, "distance");
    }


    @Override
    public List<CrownableArtist> getList(NumberParameters<ChuuDataParams> outerParams) {
        ChuuDataParams params = outerParams.getInnerParams();
        Long guildId = params.getE().isFromGuild() ? outerParams.hasOptional("server") ? params.getE().getGuild().getIdLong() : null : null;
        boolean onlySecond = outerParams.hasOptional("secondonly") || outerParams.hasOptional("second") || outerParams.hasOptional("onlysecond");
        int crownDistance = Math.toIntExact(outerParams.getExtraParam());
        return db.getCrownable(params.getLastFMData().getDiscordId(), guildId, crownDistance != Integer.MAX_VALUE || outerParams.hasOptional("nofirst"), onlySecond, crownDistance);
    }

    @Override
    public void printList(List<CrownableArtist> list, NumberParameters<ChuuDataParams> outerParmams) {
        Context e = outerParmams.getE();
        ChuuDataParams params = outerParmams.getInnerParams();
        if (list.isEmpty()) {
            sendMessageQueue(e, "Found no users :(");
            return;
        }
        boolean isServer = outerParmams.hasOptional("server") && e.isFromGuild();

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder().setColor(ColorService.computeColor(e))
                .setThumbnail(isServer ? e.getGuild().getIconUrl() : e.getJDA().getSelfUser().getAvatarUrl());
        StringBuilder a = new StringBuilder();
        List<String> lines = list.stream().map(x ->
                String.format(". [%s](%s) - **%d**/**%d** with **%d plays** %s%n",
                        CommandUtil.cleanMarkdownCharacter(x.getArtistName()),
                        LinkUtils.getLastFmArtistUrl(x.getArtistName()),
                        x.getRank(),
                        x.getTotalListeners(),
                        x.getPlayNumber(),
                        x.getRank() != 1 ? "(need " + (x.getMaxPlaynumber() - x.getPlayNumber() + 1) + " more plays for first)" : "")
        ).toList();
        for (int i = 0; i < 10 && i < lines.size(); i++) {
            a.append(i + 1).append(lines.get(i));
        }
        String s;
        if (isServer) {
            s = CommandUtil.cleanMarkdownCharacter(e.getGuild().getName());
        } else {
            s = params.getE().getJDA().getSelfUser().getName();
        }
        boolean onlySecond = outerParmams.hasOptional("secondonly") || outerParmams.hasOptional("second") || outerParmams.hasOptional("onlysecond");

        String thumbnail = isServer && e.isFromGuild() ? e.getGuild().getIconUrl() : e.getJDA().getSelfUser().getAvatarUrl();
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoNotStripped(params.getE(), params.getLastFMData().getDiscordId());
        String footer;
        String conditionalFiltering = outerParmams.getExtraParam() != Integer.MAX_VALUE ? " and you are less than " + outerParmams.getExtraParam() + " plays away from first" : "";
        if (onlySecond) {
            footer = String.format("Displaying artist where %s is the second top listener%s in %s", uInfo.getUsername(), conditionalFiltering, s);
        } else if (outerParmams.hasOptional("nofirst")) {
            footer = String.format("Displaying artist where %s is yet to be the top listener%s in %s", uInfo.getUsername(), conditionalFiltering, s);
        } else {
            footer = String.format("Displaying rank of %s's artist%s in %s", uInfo.getUsername(), conditionalFiltering, s);
        }
        embedBuilder.setDescription(a)
                .setFooter("")
                .setAuthor(String.format("%s's artist resume in %s", (uInfo.getUsername()), s), CommandUtil.getLastFmUser(params.getLastFMData().getName()), uInfo.getUrlImage())
                .setThumbnail(thumbnail)
                .setFooter(footer);
        e.sendMessage(embedBuilder.build()).queue(message ->
                new Reactionary<>(lines, message, embedBuilder));
    }

    @Override
    public String getDescription() {
        return "The rank on your artist in this server or in the bot";
    }

    @Override
    public List<String> getAliases() {
        return List.of("artistrank", "ar");
    }

    @Override
    public String getName() {
        return "Artist Rank";
    }
}
