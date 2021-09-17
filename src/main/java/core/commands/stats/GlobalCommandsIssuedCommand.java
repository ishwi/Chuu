package core.commands.stats;

import core.apis.ExecutorsSingleton;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.*;
import core.exceptions.LastFmException;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import dao.ServiceView;
import dao.entities.PrivacyUserCount;
import dao.entities.Rank;
import dao.entities.UsersWrapper;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GlobalCommandsIssuedCommand extends ConcurrentCommand<ChuuDataParams> {

    public GlobalCommandsIssuedCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_STATS;
    }

    @Override
    public Parser<ChuuDataParams> initParser() {
        return new OnlyUsernameParser(db);
    }

    @Override
    public String getDescription() {
        return "People that have run the most commands in the bot";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globalcommandslb", "globalcommandlb", "globalranlb", "globalcommandsleaderboard", "globalranleaderboard", "gcommandslb", "gcommandlb", "granlb", "gcommandsleaderboard", "granleaderboard");
    }

    @Override
    public String slashName() {
        return "commandslb";
    }

    @Override
    public String getName() {
        return "Global spammers";
    }


    @Override
    protected void onCommand(Context e, @Nonnull ChuuDataParams params) throws LastFmException, InstanceNotFoundException {

        long idLong = params.getLastFMData().getDiscordId();
        CompletableFuture<Optional<Rank<PrivacyUserCount>>> rankOpt = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            return db.getUserPosition(idLong);
        }, ExecutorsSingleton.getInstance());
        List<PrivacyUserCount> globalCommandLb = db.getGlobalCommandLb(e.getGuild().getIdLong());
        Optional<PrivacyUserCount> first = Optional.empty();
        int j = 0;
        for (PrivacyUserCount privacyUserCount : globalCommandLb) {
            j++;
            if (privacyUserCount.discordId() == idLong) {
                first = Optional.of(privacyUserCount);
                break;
            }
        }
        Optional<Rank<PrivacyUserCount>> rank;
        if (first.isPresent()) {
            rankOpt.cancel(true);
            PrivacyUserCount puC = first.get();
            rank = Optional.of(new Rank<>(globalCommandLb.get(j - 1), j));
        } else {
            try {
                rank = rankOpt.get();
            } catch (InterruptedException | ExecutionException interruptedException) {
                rank = Optional.empty();
            }
        }


        if (globalCommandLb.isEmpty()) {
            sendMessageQueue(e, e.getJDA().getSelfUser().getName() + " doesn't have any user that have ran any command!");
            return;
        }
        Set<Long> set;
        if (e.isFromGuild()) {
            set = db.getAll(e.getGuild().getIdLong()).stream().map(UsersWrapper::getDiscordID).collect(Collectors.toSet());
        } else {
            set = Set.of(idLong);
        }
        AtomicInteger atomicInteger = new AtomicInteger(1);

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setAuthor(e.getJDA().getSelfUser().getName() + "'s spammers", null, e.getGuild().getIconUrl());
        if (rank.isPresent()) {
            Rank<PrivacyUserCount> me = rank.get();
            String userString = getUserString(e, me.entity().discordId());
            embedBuilder.setFooter("%s is ranked %d%s with %d commands".formatted(userString, me.rank(), CommandUtil.getRank(me.rank()), me.entity().count()));
        }

        Function<PrivacyUserCount, String> mapper = (userListened) -> {
            PrivacyUtils.PrivateString pbStr = PrivacyUtils.getPublicString(userListened.privacyMode(), userListened.discordId(), userListened.lastfmId(), atomicInteger, e, set);
            return ". [" + pbStr.discordName() + "]" +
                   "(" + PrivacyUtils.getLastFmUser(pbStr.lastfmName()) + ")" +
                   ": " + userListened.count() + " " + CommandUtil.singlePlural(userListened.count(), "command", "commands") + "\n";
        };

        new ListSender<>(e, globalCommandLb, mapper, embedBuilder)
                .withMemoize()
                .doSend();

    }

}
