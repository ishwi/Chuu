package core.commands;

import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.OptionableParser;
import core.parsers.OptionalEntity;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import dao.entities.UsersWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AllPlayingCommand extends ConcurrentCommand {
    public AllPlayingCommand(ChuuService dao) {
        super(dao);

        this.parser = new OptionableParser(new OptionalEntity("--recent", "show last song from ALL users"));
        this.respondInPrivate = false;

    }

    @Override
    public String getDescription() {
        return ("Returns lists of all people that are playing music right now");
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("playing");
    }

    @Override
    public void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {

        String[] message = parser.parse(e);
        boolean showFresh = !Boolean.parseBoolean(message[0]);

        List<UsersWrapper> list = getService().getAll(e.getGuild().getIdLong());

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(CommandUtil.randomColor())
                .setThumbnail(e.getGuild().getIconUrl())
                .setTitle(
                        (showFresh ? "What is being played now in " : "What was being played in ")
                                + e.getGuild().getName());

        List<String> result = list.parallelStream().map(u ->
        {
            Optional<NowPlayingArtist> opt;
            try {
                opt = Optional.of(lastFM.getNowPlayingInfo(u.getLastFMName()));
            } catch (Exception ex) {
                opt = Optional.empty();
            }
            return Map.entry(u, opt);
        }).filter(x -> {
            Optional<NowPlayingArtist> value = x.getValue();
            return value.isPresent() && !(showFresh && !value.get().isNowPlaying());
        }).map(x -> {
                    UsersWrapper usersWrapper = x.getKey();
                    NowPlayingArtist value = x.getValue().get(); //Checked previous filter
                    Member member = e.getGuild().getMemberById(usersWrapper.getDiscordID());
                    String username = member == null ? usersWrapper.getLastFMName() : member.getEffectiveName();
                    String started = !showFresh && value.isNowPlaying() ? "#" : "+";
                    return started + " [" +
                           username + "](" +
                           CommandUtil.getLastFmUser(usersWrapper.getLastFMName()) +
                           "): " +
                           value.getSongName() +
                           " - " + value.getArtistName() +
                           " | " + value.getAlbumName() + "\n";
                }
        ).collect(Collectors.toList());

//		if (value.isEmpty()) {
//			return false;
//		}
//		if (showFresh) {
//			if (!value.get().isNowPlaying()) {
//				return false;
//			}
//		}

        if (result.size() == 0) {
            sendMessageQueue(e, "None is listening to music at the moment UwU");
            return;
        }
        StringBuilder a = new StringBuilder();
        int count = 0;
        for (String string : result) {
            count++;
            if ((a.length() > 1500) || (count == 20)) {
                break;
            }
            a.append(string);
        }
        int pageSize = count;
        MessageBuilder mes = new MessageBuilder();
        embedBuilder.setDescription(a);
        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                executor.execute(() -> new Reactionary<>(result, message1, pageSize, embedBuilder)));

    }

    @Override
    public String getName() {
        return "Playing";
    }


}
