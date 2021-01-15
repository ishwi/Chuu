package core.commands.scrobble;

import core.Chuu;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.config.SetCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.exceptions.DuplicateInstanceException;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.awt.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ScrobbleCommand extends ConcurrentCommand<CommandParameters> {
    private final SetCommand setCommand;

    public ScrobbleCommand(ChuuService dao) {
        super(dao);
        this.setCommand = new SetCommand(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SCROBBLING;
    }

    @Override
    public Parser<CommandParameters> initParser() {
        return new NoOpParser();
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
    protected void onCommand(MessageReceivedEvent e, @NotNull CommandParameters params) throws LastFmException, InstanceNotFoundException {
        boolean notExisting = false;
        LastFMData lastFMData = null;
        String tempUser = null;
        try {
            lastFMData = getService().findLastFMData(e.getAuthor().getIdLong());
        } catch (InstanceNotFoundException exception) {
            notExisting = true;
        }
        String authToken = lastFM.getAuthToken();
        if (!notExisting) {
            getService().storeToken(authToken, lastFMData.getName());
        }
        String s = lastFM.generateSessionUrl(authToken);
        boolean finalNotExisting = notExisting;
        LastFMData finalLastFMData = lastFMData;
        e.getAuthor().openPrivateChannel().queue(t -> {

            sendMessage(e, "Sent you a DM with the login details!").queue();
            t.sendMessage(new EmbedBuilder().setColor(CommandUtil.randomColor()).setTitle("Last.fm login").setDescription(String.format("**[%s](%s)**", "Follow this link to complete the login", s)).build()).queue(z -> {

                ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
                AtomicInteger counter = new AtomicInteger();
                scheduledExecutor.scheduleWithFixedDelay(() -> {
                    counter.incrementAndGet();
                    if (counter.get() >= 25) {
                        getService().storeSess(null, authToken);
                        scheduledExecutor.shutdown();
                        z.editMessage(new EmbedBuilder().setTitle("Link expired").setColor(Color.red).build()).queue();
                    }

                    try {

                        String session = lastFM.findSession(authToken);
                        String userAccount = lastFM.findUserAccount(session);

                        if (!finalNotExisting) {
                            if (userAccount.equalsIgnoreCase(finalLastFMData.getName())) {
                                getService().storeSess(session, finalLastFMData.getName());
                                z.editMessage(new EmbedBuilder().setColor(Color.green).setTitle(":white_check_mark: Succesfully logged in!").build()).queue();
                                scheduledExecutor.shutdown();
                                return;
                            } else {
                                t.sendMessage("You had previously logged in with a different account. Will reset eveything now").queue();
                                try {
                                    getService().changeLastFMName(e.getAuthor().getIdLong(), userAccount);
                                    getService().storeSess(session, userAccount);
                                } catch (DuplicateInstanceException duplicateInstanceException) {
                                    getService().removeUserCompletely(e.getAuthor().getIdLong());
                                    getService().changeDiscordId(e.getAuthor().getIdLong(), userAccount);
                                    getService().storeSess(session, userAccount);
                                } catch (InstanceNotFoundException duplicateInstanceException) {
                                    Chuu.getLogger().warn("infe shouldnt happen here {}", duplicateInstanceException.getMessage());
                                    t.sendMessage("Something unusual happpened, try again later :(").queue();
                                    scheduledExecutor.shutdown();
                                    return;
                                }
                                setCommand.setProcess(t, userAccount, e.getAuthor().getIdLong(), LastFMData.ofUser(userAccount), e.getAuthor().getName());
                                scheduledExecutor.shutdown();

                            }
                        }

                        LastFMData newUser = LastFMData.ofUser(userAccount);
                        if (e.isFromGuild()) {
                            newUser.setGuildID(e.getGuild().getIdLong());
                        }
                        try {
                            getService().getDiscordIdFromLastfm(userAccount);
                            // Exists
                            getService().removeUserCompletely(e.getAuthor().getIdLong());
                            getService().changeDiscordId(e.getAuthor().getIdLong(), userAccount);
                            getService().storeSess(session, userAccount);

                        } catch (InstanceNotFoundException ex) {
                            newUser.setDiscordId(e.getAuthor().getIdLong());
                            getService().insertNewUser(newUser);
                            getService().storeSess(session, userAccount);
                            z.editMessage(new EmbedBuilder().setTitle(":white_check_mark: Succesfully logged in!").setDescription("Now will try to index your library").setColor(Color.green).build()).queue();
                            setCommand.setProcess(t, userAccount, e.getAuthor().getIdLong(), LastFMData.ofUser(userAccount), e.getAuthor().getName());
                            scheduledExecutor.shutdown();
                            return;
                        }
                        scheduledExecutor.shutdown();
                        z.editMessage(new EmbedBuilder().setTitle(":white_check_mark: Succesfully logged in!").setColor(Color.green).build()).queue();
                    } catch (LastFmException instanceNotFoundException) {
                        instanceNotFoundException.printStackTrace();
                    }

                }, 20, 30, TimeUnit.SECONDS);
            });


        }, error -> e.getChannel().

                sendMessage("Cannot send DM's to you. Please enable DMs if you want to use this option").

                queue());

    }
}
