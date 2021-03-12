package core;

import com.google.common.util.concurrent.RateLimiter;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.SpotifySingleton;
import core.commands.CustomInterfacedEventManager;
import core.commands.abstracts.MyCommand;
import core.commands.config.HelpCommand;
import core.commands.config.PrefixCommand;
import core.commands.discovery.FeaturedCommand;
import core.commands.moderation.AdministrativeCommand;
import core.commands.moderation.EvalCommand;
import core.commands.moderation.TagWithYearCommand;
import core.music.ExtendedAudioPlayerManager;
import core.music.PlayerRegistry;
import core.music.listeners.VoiceListener;
import core.otherlisteners.AwaitReady;
import core.otherlisteners.ConstantListener;
import core.scheduledtasks.ArtistMbidUpdater;
import core.scheduledtasks.ImageUpdaterThread;
import core.scheduledtasks.SpotifyUpdaterThread;
import core.scheduledtasks.UpdaterThread;
import core.services.ColorService;
import core.services.CoverService;
import core.services.MessageDeletionService;
import core.services.MessageDisablingService;
import dao.ChuuService;
import dao.entities.Metrics;
import dao.exceptions.ChuuServiceException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class Chuu {

    public static final Character DEFAULT_PREFIX = '!';
    public static final String DEFAULT_LASTFM_ID = "chuubot";
    private static final LongAdder lastFMMetric = new LongAdder();
    private static final Set<String> privateLastFms = new HashSet<>();

    private static ShardManager shardManager;
    private static Logger logger;
    private static ScheduledExecutorService scheduledExecutorService;
    private static Map<Long, RateLimiter> ratelimited;
    private static Map<Long, Character> prefixMap;
    private static ChuuService dao;
    private static CoverService coverService;
    private static MessageDeletionService messageDeletionService;
    private static MessageDisablingService messageDisablingService = new MessageDisablingService();
    public static PlayerRegistry playerRegistry;
    public static ExtendedAudioPlayerManager playerManager;
    public static String chuuSess;
    public static String ipv6Block;
    public static long channelId;
    public static long channel2Id;


    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }


    public static void addGuildPrefix(long guildId, Character prefix) {
        if (prefix.equals(DEFAULT_PREFIX)) {
            prefixMap.remove(guildId);
        } else {
            Character replace = prefixMap.replace(guildId, prefix);
            if (replace == null) {
                prefixMap.put(guildId, prefix);
            }
        }
    }

    //Returns if its disabled or enabled now


    public static Character getCorrespondingPrefix(MessageReceivedEvent e) {
        if (!e.isFromGuild())
            return DEFAULT_PREFIX;
        long id = e.getGuild().getIdLong();
        Character character = prefixMap.get(id);
        return character == null ? DEFAULT_PREFIX : character;

    }

    public static Map<Long, Character> getPrefixMap() {
        return prefixMap;
    }

    public static String getLastFmId(String lastfmId) {
        if (privateLastFms.contains(lastfmId)) {
            return DEFAULT_LASTFM_ID;
        }
        return lastfmId;
    }

    public static void incrementMetric() {
        lastFMMetric.increment();
    }

    public static void main(String[] args) throws InterruptedException {
        if (System.getProperty("file.encoding").equals("UTF-8")) {
            setupBot(Arrays.stream(args).anyMatch(x -> x.equalsIgnoreCase("stop-asking")));
        } else {
            relaunchInUTF8();
        }
    }

    private static void relaunchInUTF8() throws InterruptedException {
        System.out.println("BotLauncher: We are not running in UTF-8 mode! This is a problem!");
        System.out.println("BotLauncher: Relaunching in UTF-8 mode using -Dfile.encoding=UTF-8");

        String[] command = new String[]{"java", "-Dfile.encoding=UTF-8", "--enable-preview", "-jar",
                Chuu.getThisJarFile().getAbsolutePath()};

        // Relaunches the bot using UTF-8 mode.
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO(); // Tells the new process to use the same command line as this one.
        try {
            Process process = processBuilder.start();
            process.waitFor(); // We wait here until the actual bot stops. We do this so that we can keep using
            // the same command line.
            System.exit(process.exitValue());
        } catch (IOException e) {
            if (e.getMessage().contains("\"java\"")) {
                System.out.println(
                        "BotLauncher: There was an error relaunching the bot. We couldn't find Java to launch with.");
                System.out.println(
                        "BotLauncher: Attempted to relaunch using the command:\n   " + String.join(" ", command));
                System.out.println(
                        "BotLauncher: Make sure that you have Java properly set in your Operating System's PATH variable.");
                System.out.println("BotLauncher: Stopping here.");
            } else {
                Chuu.getLogger().warn(e.getMessage(), e);
            }
        }
    }

    private static File getThisJarFile() {
        // Gets the path of the currently running Jar file
        String path = Chuu.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);

        // This is code especially written for running and testing this program in an
        // IDE that doesn't compile to .jar when running.
        if (!decodedPath.endsWith(".jar"))
            return new File("Chuu.jar");
        return new File(decodedPath); // We use File so that when we send the path to the ProcessBuilder, we will be
        // using the proper System path formatting.
    }

    private static Collection<GatewayIntent> getIntents() {
        EnumSet<GatewayIntent> gatewayIntents = GatewayIntent.fromEvents(GuildMemberRemoveEvent.class,
                GuildMessageReactionAddEvent.class,
                PrivateMessageReactionAddEvent.class,
                MessageReceivedEvent.class
        );
        gatewayIntents.add(GatewayIntent.GUILD_VOICE_STATES);
        gatewayIntents.add(GatewayIntent.GUILD_EMOJIS);
        return gatewayIntents;
    }

    public static void setupBot(boolean isTest) {
        logger = LoggerFactory.getLogger(Chuu.class);
        Properties properties = readToken();
        String channel = properties.getProperty("MODERATION_CHANNEL_ID");
        String channel2 = properties.getProperty("MODERATION_CHANNEL_2_ID");
        channelId = Long.parseLong(channel);
        channel2Id = Long.parseLong(channel2);
        chuuSess = properties.getProperty("LASTFM_BOT_SESSION_KEY");
        ipv6Block = properties.getProperty("IPV6_BLOCK");
        dao = new ChuuService();
        prefixMap = initPrefixMap(dao);
        ColorService.init(dao);
        coverService = new CoverService(dao);
        DiscogsSingleton.init(properties.getProperty("DC_SC"), properties.getProperty("DC_KY"));
        SpotifySingleton.init(properties.getProperty("client_ID"), properties.getProperty("client_Secret"));
        playerManager = new ExtendedAudioPlayerManager();
        playerRegistry = new PlayerRegistry(playerManager);
        scheduledExecutorService = Executors.newScheduledThreadPool(4);

        // Needs these three references
        HelpCommand help = new HelpCommand(dao);
        AdministrativeCommand commandAdministrator = new AdministrativeCommand(dao);
        PrefixCommand prefixCommand = new PrefixCommand(dao);
        TagWithYearCommand tagWithYearCommand = new TagWithYearCommand(dao);
        EvalCommand evalCommand = new EvalCommand(dao);
        FeaturedCommand featuredCommand = new FeaturedCommand(dao, scheduledExecutorService);

        // Logs every fime minutes the api calls
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            long l = lastFMMetric.longValue();
            lastFMMetric.reset();
            dao.updateMetric(Metrics.LASTFM_PETITIONS, l);
            logger.info("Made {} petitions in the last 5 minutes", l);
        }, 5, 5, TimeUnit.MINUTES);
        ratelimited = dao.getRateLimited().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, y -> RateLimiter.create(y.getValue())));


        MessageAction.setDefaultMentions(EnumSet.noneOf(Message.MentionType.class));
        MessageAction.setDefaultMentionRepliedUser(false);


        AtomicInteger counter = new AtomicInteger(0);
        IEventManager customManager = new CustomInterfacedEventManager(0);
        EnumSet<CacheFlag> cacheFlags = EnumSet.allOf(CacheFlag.class);
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.create(getIntents())
                .setChunkingFilter(ChunkingFilter.ALL)
                //.setMemberCachePolicy(Chuu.cacheMember)
                .enableCache(CacheFlag.EMOTE)
                .enableCache(CacheFlag.VOICE_STATE)
                .setAudioSendFactory(new NativeAudioSendFactory())

                .setBulkDeleteSplittingEnabled(false)
                .setToken(properties.getProperty("DISCORD_TOKEN")).setAutoReconnect(true)
                .setEventManagerProvider(a -> customManager)
                .addEventListeners(help)
                .setShardsTotal(-1)
                .addEventListeners(help.registerCommand(commandAdministrator))
                .addEventListeners(help.registerCommand(prefixCommand))
                .addEventListeners(new VoiceListener())
                .addEventListeners(help.registerCommand(tagWithYearCommand))
                .addEventListeners(help.registerCommand(featuredCommand))
                .addEventListeners(new ConstantListener(channelId, dao))
                .addEventListeners((Object[]) scanListeners(help))
                .addEventListeners(new AwaitReady(counter, (ShardManager shard) -> {
                    messageDisablingService = new MessageDisablingService(shard, dao);
                    prefixCommand.onStartup(shard);
                    if (!isTest) {
                        commandAdministrator.onStartup(shardManager);
                    }

                    evalCommand.setOwnerId(shard);
                    shardManager.addEventListener(help.registerCommand(new FeaturedCommand(dao, scheduledExecutorService)));
                    updatePresence("Chuu");
                }));


        try {
            initPrivateLastfms(dao);
            messageDeletionService = new MessageDeletionService(dao.getServersWithDeletableMessages());
            shardManager = builder.build();
            scheduledExecutorService.scheduleAtFixedRate(
                    new UpdaterThread(dao, true), 0, 120,
                    TimeUnit.SECONDS);

            scheduledExecutorService.scheduleAtFixedRate(new ImageUpdaterThread(dao), 20, 12, TimeUnit.MINUTES);
            scheduledExecutorService.scheduleAtFixedRate(
                    new SpotifyUpdaterThread(dao), 5, 5, TimeUnit.MINUTES);
            scheduledExecutorService.scheduleAtFixedRate(new ArtistMbidUpdater(dao), 10, 2000, TimeUnit.MINUTES);
        } catch (LoginException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
    }

    private static MyCommand<?>[] scanListeners(HelpCommand help) {
        try (ScanResult result = new ClassGraph().acceptPackages("core.commands").scan()) {
            return result.getAllClasses().stream().filter(x -> x.isStandardClass() && !x.isAbstract())
                    .filter(x -> !x.getSimpleName().equals(HelpCommand.class.getSimpleName())
                            && !x.getSimpleName().equals(AdministrativeCommand.class.getSimpleName())
                            && !x.getSimpleName().equals(PrefixCommand.class.getSimpleName())
                            && !x.getSimpleName().equals(TagWithYearCommand.class.getSimpleName())
                            && !x.getSimpleName().equals(EvalCommand.class.getSimpleName())
                            && !x.getSimpleName().equals(FeaturedCommand.class.getSimpleName()))
                    .filter(x -> x.extendsSuperclass("core.commands.abstracts.MyCommand"))
                    .map(x -> {
                        try {
                            return (MyCommand<?>) x.loadClass().getConstructor(ChuuService.class).newInstance(dao);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            throw new ChuuServiceException(e);
                        }

                    })
                    .peek(help::registerCommand)
                    .toArray(MyCommand<?>[]::new);
        } catch (Exception ex) {
            logger.error("There was an error while registering the commands!", ex);
            throw new ChuuServiceException(ex);
        }
    }

    private static void initPrivateLastfms(ChuuService dao) {
        privateLastFms.addAll(dao.getPrivateLastfmIds());
    }

    public static void changePrivacyLastfm(String lastfmId, boolean mode) {
        if (mode) {
            privateLastFms.add(lastfmId);
        } else {
            privateLastFms.remove(lastfmId);
        }
    }


    public static Logger getLogger() {
        return logger;
    }

    private static Map<Long, Character> initPrefixMap(ChuuService dao) {
        return (dao.getGuildPrefixes(Chuu.DEFAULT_PREFIX));
    }

    public static Properties readToken() {

        Properties properties = new Properties();
        try (InputStream in = Chuu.class.getResourceAsStream("/all.properties")) {
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new ChuuServiceException(e);
        }
    }

    public static void updatePresence(String artist) {

        Chuu.shardManager.getShards().forEach(x -> x.getPresence().setActivity(Activity.playing(artist + " | !help for help")));

    }

    public static Map<Long, RateLimiter> getRatelimited() {
        return ratelimited;
    }

    public static ShardManager getShardManager() {
        return shardManager;
    }

    public static ChuuService getDao() {
        return dao;
    }

    public static MessageDeletionService getMessageDeletionService() {
        return messageDeletionService;
    }

    public static MessageDisablingService getMessageDisablingService() {
        return messageDisablingService;
    }

    public static String getRandomSong(String name) {

        return null;
    }

    public static CoverService getCoverService() {
        return coverService;
    }

    public static boolean isLoaded() {
        return shardManager.getShardsRunning() == shardManager.getShardsTotal();
    }
/*
    public static MemberCachePolicy cacheMember = Chuu::caching;

    public static boolean caching(Member l) {
        Chuu.logger.warn("testing user " + l.getEffectiveName());

        Member prevOccurence = l.getGuild().getMemberCache().getElementById(l.getId());
        if (prevOccurence != null) {
            Chuu.logger.warn("Member already on cache " + l.getEffectiveName());
            return true;
        }
        try {
            dao.findLastFMData(l.getUser().getIdLong());
            Chuu.logger.warn("Member added " + l.getEffectiveName());
            return true;
        } catch (InstanceNotFoundException exception) {
            Chuu.logger.warn("Rejected " + l.getEffectiveName());
            return false;
        }
    }*/
}
