package core.commands.scrobble;

import core.Chuu;
import core.commands.Context;
import core.commands.ContextMessageReceived;
import core.commands.ContextSlashReceived;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.config.SetCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import core.util.ChuuVirtualPool;
import dao.ServiceView;
import dao.entities.LastFMData;
import dao.exceptions.DuplicateInstanceException;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class LoginCommand extends ConcurrentCommand<CommandParameters> {
    private final SetCommand setCommand;

    public LoginCommand(ServiceView dao) {
        super(dao);
        this.setCommand = new SetCommand(dao);
        this.ephemeral = true;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.STARTING;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return NoOpParser.INSTANCE;
    }

    @Override
    public String getDescription() {
        return "Login your last.fm account";
    }

    @Override
    public List<String> getAliases() {
        return List.of("login");
    }

    @Override
    public String getName() {
        return "Login";
    }

    @Override
    public void onCommand(Context e, @Nonnull CommandParameters params) throws LastFmException {
        boolean notExisting = false;
        LastFMData lastFMData = null;
        long authorId = e.getAuthor().getIdLong();
        try {
            lastFMData = db.findLastFMData(authorId);
        } catch (InstanceNotFoundException exception) {
            notExisting = true;
        }
        String authToken = lastFM.getAuthToken();
        if (!notExisting) {
            db.storeToken(authToken, lastFMData.getName());
        }
        String s = lastFM.generateSessionUrl(authToken);
        boolean finalNotExisting = notExisting;
        LastFMData finalLastFMData = lastFMData;
        MessageEmbed build = new ChuuEmbedBuilder(e).setTitle("Last.fm login").setDescription(String.format("**[%s](%s)**", "Follow this link to complete the login", s)).build();
        RestAction<Message> action;
        Consumer<EmbedBuilder> consume;
        Consumer<String> consumeStr;
        if (e instanceof ContextSlashReceived csr) {
            action = e.sendMessage(build);
            consume = (c) -> e.editMessage(null, c.build(), Collections.emptyList()).queue();
            consumeStr = (c) -> csr.e().reply(c).setEphemeral(true).queue();
        } else {
            consume = (c) -> e.sendMessage(c.build()).queue();
            consumeStr = (c) -> e.sendMessage(c).queue();
            action = e.getAuthor().openPrivateChannel().flatMap(t ->
                    t.sendMessageEmbeds(build));
        }
        action.queue(a -> {
            if (e instanceof ContextMessageReceived t) {
                sendMessage(e, "Sent you a DM with the login details!").queue();
            }
            try (ScheduledExecutorService scheduledExecutor = ChuuVirtualPool.ofScheduled("Login")) {
                AtomicInteger counter = new AtomicInteger();
                scheduledExecutor.scheduleWithFixedDelay(() -> {
                    counter.incrementAndGet();
                    if (counter.get() >= 25) {
                        db.storeSess(null, authToken);
                        scheduledExecutor.shutdown();
                        consume.accept(new ChuuEmbedBuilder(e).setTitle("Link expired").setDescription("Try to run the command again if you want to authorize the bot!").setColor(Color.red));
                    }

                    try {

                        String session = lastFM.findSession(authToken);
                        String userAccount = lastFM.findUserAccount(session);

                        String authorName = e.getAuthor().getName();
                        if (!finalNotExisting) {
                            if (userAccount.equalsIgnoreCase(finalLastFMData.getName())) {
                                db.storeSess(session, finalLastFMData.getName());
                                consume.accept(new ChuuEmbedBuilder(e).setColor(Color.green).setTitle(":white_check_mark: Successfully logged in!"));
                                scheduledExecutor.shutdown();
                                return;
                            } else {
                                consumeStr.accept("You had previously logged in with a different account. Will reset eveything now");
                                try {
                                    db.changeLastFMName(authorId, userAccount);
                                    db.storeSess(session, userAccount);
                                } catch (DuplicateInstanceException duplicateInstanceException) {
                                    db.removeUserCompletely(authorId);
                                    db.changeDiscordId(authorId, userAccount);
                                    db.storeSess(session, userAccount);
                                } catch (InstanceNotFoundException duplicateInstanceException) {
                                    Chuu.getLogger().warn("infe shouldnt happen here {}", duplicateInstanceException.getMessage());
                                    consumeStr.accept("Something unusual happpened, try again later :(");
                                    scheduledExecutor.shutdown();
                                    return;
                                }
                                setCommand.setProcess(e, userAccount, authorId, LastFMData.ofUser(userAccount), authorName);
                                scheduledExecutor.shutdown();

                            }
                        }


                        try {
                            db.getDiscordIdFromLastfm(userAccount);
                            // Exists
                            db.removeUserCompletely(authorId);
                            db.changeDiscordId(authorId, userAccount);
                            db.storeSess(session, userAccount);

                        } catch (InstanceNotFoundException ex) {
                            LastFMData newUser = LastFMData.ofUser(userAccount);
                            if (e.isFromGuild()) {
                                newUser.setGuildID(e.getGuild().getIdLong());
                            }
                            newUser.setDiscordId(authorId);
                            db.insertNewUser(newUser);
                            Chuu.refreshCache(authorId);
                            db.storeSess(session, userAccount);
                            consume.accept(new ChuuEmbedBuilder(e).setTitle(":white_check_mark: Successfully logged in!").setDescription("Now I will try to index your library").setColor(Color.green));
                            setCommand.setProcess(e, userAccount, authorId, LastFMData.ofUser(userAccount), authorName);
                            scheduledExecutor.shutdown();
                            return;
                        }
                        scheduledExecutor.shutdown();
                        consume.accept(new ChuuEmbedBuilder(e).setTitle(":white_check_mark: Successfully logged in!").setColor(Color.green));
                    } catch (LastFmException instanceNotFoundException) {
                        instanceNotFoundException.printStackTrace();
                    }

                }, 6, 5, TimeUnit.SECONDS);
            }
        }, error ->
        {
            if (e instanceof ContextMessageReceived ctmr) {
                e.getChannel().sendMessage("Cannot send DMs to you. Use the slash command version (`/login`) or enable DMs if you want to use this option").queue();
            }
        });

    }
}
