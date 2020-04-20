package core.commands;

import core.otherlisteners.Reactionary;
import core.parsers.OnlyUsernameParser;
import core.parsers.OptionalEntity;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ChuuService;
import dao.entities.CrownableArtist;
import dao.entities.DiscordUserDisplay;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.stream.Collectors;

public class CrownableCommand extends ListCommand<CrownableArtist, ChuuDataParams> {

    public CrownableCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    public Parser<ChuuDataParams> getParser() {
        OnlyUsernameParser onlyUsernameParser = new OnlyUsernameParser(getService());
        onlyUsernameParser.addOptional(new OptionalEntity("--nofirst", "To show only the artists in which you are not first"));
        onlyUsernameParser.addOptional(new OptionalEntity("--server", "to make the ranking only count for this server"));
        return onlyUsernameParser;
    }

    @Override
    public List<CrownableArtist> getList(ChuuDataParams params) {
        Long guildId = params.getE().isFromGuild() ? params.hasOptional("--server") ? params.getE().getGuild().getIdLong() : null : null;
        return getService().getCrownable(params.getLastFMData().getDiscordId(), guildId, params.hasOptional("--todo"));
    }

    @Override
    public void printList(List<CrownableArtist> list, ChuuDataParams params) {
        boolean isServer = params.hasOptional("--server");

        MessageReceivedEvent e = params.getE();
        MessageBuilder messageBuilder = new MessageBuilder();

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(e.getGuild().getIconUrl());
        StringBuilder a = new StringBuilder();

        if (list.isEmpty()) {
            sendMessageQueue(e, "Found no registered users :(");
            return;
        }
        List<String> collect = list.stream().map(x ->
                String.format(". [%s](%s) - **%d**/**%d** with **%d plays** %s%n",
                        CommandUtil.cleanMarkdownCharacter(x.getArtistName()),
                        CommandUtil.getLastFmArtistUrl(x.getArtistName()),
                        x.getRank(),
                        x.getTotalListeners(),
                        x.getPlayNumber(),
                        x.getRank() != 1 ? "(need " + (x.getMaxPlaynumber() - x.getPlayNumber()) + " more plays for first)" : "")
        ).collect(Collectors.toList());
        for (int i = 0; i < 10 && i < collect.size(); i++) {
            a.append(i + 1).append(collect.get(i));
        }
        String s;
        if (isServer) {
            s = CommandUtil.cleanMarkdownCharacter(e.getGuild().getName());
        } else {
            s = params.getE().getJDA().getSelfUser().getName();
        }
        String thumbnail = isServer && e.isFromGuild() ? e.getGuild().getIconUrl() : e.getJDA().getSelfUser().getAvatarUrl();
        DiscordUserDisplay uInfo = CommandUtil.getUserInfoConsideringGuildOrNot(params.getE(), params.getLastFMData().getDiscordId());
        embedBuilder.setDescription(a)
                .setAuthor(String.format("%s's crown resume in %s", uInfo.getUsername(), s), CommandUtil.getLastFmUser(params.getLastFMData().getName()), uInfo.getUrlImage())
                .setThumbnail(thumbnail);
        messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue(message ->
                new Reactionary<>(collect, message, embedBuilder));
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
