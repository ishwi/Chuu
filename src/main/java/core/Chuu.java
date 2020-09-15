package core;

import com.google.common.util.concurrent.RateLimiter;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.SpotifySingleton;
import core.commands.*;
import core.exceptions.ChuuServiceException;
import core.otherlisteners.AwaitReady;
import core.scheduledtasks.ArtistMbidUpdater;
import core.scheduledtasks.ImageUpdaterThread;
import core.scheduledtasks.SpotifyUpdaterThread;
import core.scheduledtasks.UpdaterThread;
import dao.ChuuService;
import dao.entities.Metrics;
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
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public class Chuu {

    public static final Character DEFAULT_PREFIX = '!';
    private static ShardManager shardManager;
    private static Logger logger;
    private static ScheduledExecutorService scheduledExecutorService;
    private static final LongAdder lastFMMetric = new LongAdder();
    private static Map<Long, RateLimiter> ratelimited;
    private static Map<Long, Character> prefixMap;
    private static final Set<String> privateLastFms = new HashSet<>();
    public static final String DEFAULT_LASTFM_ID = "chuubot";
    public static final MultiValuedMap<Long, MyCommand<?>> disabledServersMap = new HashSetValuedHashMap<>();
    public final static MultiValuedMap<Pair<Long, Long>, MyCommand<?>> disabledChannelsMap = new HashSetValuedHashMap<>();
    public final static MultiValuedMap<Pair<Long, Long>, MyCommand<?>> enabledChannelsMap = new HashSetValuedHashMap<>();
    private static ChuuService dao;


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
    public static void toggleCommandChannelDisabledness(MyCommand<?> myCommand, long guildId, long channelId, boolean expectedResult, ChuuService service) {
        Pair<Long, Long> channel = Pair.of(guildId, channelId);
        boolean serverSet = disabledServersMap.containsMapping(guildId, myCommand);
        if (expectedResult) {
            disabledServersMap.removeMapping(channel, myCommand);
            service.deleteChannelCommandStatus(guildId, channelId, myCommand.getName());
            if (serverSet) {
                enabledChannelsMap.put(channel, myCommand);
                service.insertChannelCommandStatus(guildId, channelId, myCommand.getName(), true);

            }  //Do Nothing

            // If this command was disabled server wide
        } else {
            enabledChannelsMap.removeMapping(channel, myCommand);
            service.deleteChannelCommandStatus(guildId, channelId, myCommand.getName());
            if (!serverSet) {
                disabledChannelsMap.put(channel, myCommand);
                service.insertChannelCommandStatus(guildId, channelId, myCommand.getName(), false);

            }
        }
    }


    public static boolean isMessageAllowed(MyCommand<?> command, MessageReceivedEvent e) {
        if (!e.isFromGuild()) {
            return true;
        }
        long guildId = e.getGuild().getIdLong();
        long channelId = e.getChannel().getIdLong();
        return (!(disabledServersMap.get(guildId).contains(command) || disabledChannelsMap.get(Pair.of(guildId, channelId)).contains(command)))
                || enabledChannelsMap.get(Pair.of(guildId, channelId)).contains(command);
    }

    //Returns if its disabled or enabled now
    public static void toggleCommandDisabledness(MyCommand<?> myCommand, long guildId, boolean expectedResult, ChuuService service) {
        if (expectedResult) {
            disabledServersMap.removeMapping(guildId, myCommand);
            service.deleteServerCommandStatus(guildId, myCommand.getName());

            Set<Long> collect = disabledChannelsMap.entries().stream().filter(x -> x.getKey().getLeft().equals(guildId)).map(x -> x.getKey().getRight()).collect(Collectors.toSet());
            disabledChannelsMap.entries().removeIf(x -> x.getKey().getLeft().equals(guildId));
            collect.forEach(y -> service.deleteChannelCommandStatus(guildId, y, myCommand.getName()));
            service.deleteServerCommandStatus(guildId, myCommand.getName());

        } else {
            disabledServersMap.put(guildId, myCommand);
            service.insertServerDisabled(guildId, myCommand.getName());
        }
    }

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
        return GatewayIntent.fromEvents(GuildMemberRemoveEvent.class,
                GuildMessageReactionAddEvent.class,
                PrivateMessageReactionAddEvent.class,
                MessageReceivedEvent.class
        );
    }

    public static void setupBot(boolean isTest) {
        logger = LoggerFactory.getLogger(Chuu.class);
        Properties properties = readToken();
        dao = new ChuuService();
        prefixMap = initPrefixMap(dao);
        DiscogsSingleton.init(properties.getProperty("DC_SC"), properties.getProperty("DC_KY"));
        SpotifySingleton.init(properties.getProperty("client_ID"), properties.getProperty("client_Secret"));

        // Needs these three references
        HelpCommand help = new HelpCommand(dao);
        AdministrativeCommand commandAdministrator = new AdministrativeCommand(dao);
        PrefixCommand prefixCommand = new PrefixCommand(dao);

        scheduledExecutorService = Executors.newScheduledThreadPool(4);
        // Logs every fime minutes the api calls
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            long l = lastFMMetric.longValue();
            dao.updateMetric(Metrics.LASTFM_PETITIONS, l);
            lastFMMetric.reset();
            logger.info("Made {} petitions in the last 5 minutes", l);
        }, 5, 5, TimeUnit.MINUTES);
        ratelimited = dao.getRateLimited().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, y -> RateLimiter.create(y.getValue())));

        MessageAction.setDefaultMentions(EnumSet.noneOf(Message.MentionType.class));

        AtomicInteger counter = new AtomicInteger(0);
        IEventManager customManager = new CustomInterfacedEventManager(0);
        EvalCommand evalCommand = new EvalCommand(dao);
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.create(getIntents()).setChunkingFilter(ChunkingFilter.ALL)
                //.setMemberCachePolicy(Chuu.cacheMember)
                .disableCache(EnumSet.allOf(CacheFlag.class))
                .setBulkDeleteSplittingEnabled(false)
                .setToken(properties.getProperty("DISCORD_TOKEN")).setAutoReconnect(true)
                .setEventManagerProvider(a -> customManager)
                .addEventListeners(help)
                .setShardsTotal(-1)
                .addEventListeners(help.registerCommand(commandAdministrator))
                .addEventListeners(help.registerCommand(new NowPlayingCommand(dao)))
                .addEventListeners(help.registerCommand(new WhoKnowsCommand(dao)))
                // .addEventListeners(help.registerCommand(new WhoKnowsNPCommand(dao)))
                .addEventListeners(help.registerCommand(new AlbumChartCommand(dao)))
                .addEventListeners(help.registerCommand(new SetCommand(dao)))
                .addEventListeners(help.registerCommand(new PlayingCommand(dao)))
                .addEventListeners(help.registerCommand(new TasteCommand(dao)))
                .addEventListeners(help.registerCommand(new TopCommand(dao)))
                .addEventListeners(help.registerCommand(new UpdateCommand(dao)))
                .addEventListeners(help.registerCommand(new NPSpotifyCommand(dao)))
                .addEventListeners(help.registerCommand(new UniqueCommand(dao)))
                .addEventListeners(help.registerCommand(new ArtistCommand(dao)))
                .addEventListeners(help.registerCommand(new AlbumPlaysCommand(dao)))
                .addEventListeners(help.registerCommand(new GuildTopCommand(dao)))
                .addEventListeners(help.registerCommand(new ArtistUrlCommand(dao)))
                .addEventListeners(help.registerCommand(new BandInfoCommand(dao)))
                // .addEventListeners(help.registerCommand(new BandInfoNpCommand(dao)))
                .addEventListeners(help.registerCommand(new LocalWhoKnowsAlbumCommand(dao)))
                .addEventListeners(help.registerCommand(new CrownLeaderboardCommand(dao)))
                .addEventListeners(help.registerCommand(new CrownsCommand(dao)))
                .addEventListeners(help.registerCommand(new RecentListCommand(dao)))
                .addEventListeners(help.registerCommand(new MusicBrainzCommand(dao)))
                .addEventListeners(help.registerCommand(new GenreCommand(dao)))
                .addEventListeners(help.registerCommand(new MbizThisYearCommand(dao)))
                .addEventListeners(help.registerCommand(new ArtistPlaysCommand(dao)))
                .addEventListeners(help.registerCommand(new TimeSpentCommand(dao)))
                .addEventListeners(help.registerCommand(new YoutubeSearchCommand(dao)))
                .addEventListeners(help.registerCommand(new UniqueLeaderboardCommand(dao)))
                .addEventListeners(help.registerCommand(new ProfileInfoCommand(dao)))
                .addEventListeners(help.registerCommand(new ArtistCountLeaderboard(dao)))
                .addEventListeners(help.registerCommand(new TotalArtistNumberCommand(dao)))
                .addEventListeners(help.registerCommand(new CountryCommand(dao)))
                .addEventListeners(help.registerCommand(new AlbumTracksDistributionCommand(dao)))
                .addEventListeners(help.registerCommand(new ObscurityLeaderboardCommand(dao)))
                .addEventListeners(help.registerCommand(new RandomAlbumCommand(dao)))
                .addEventListeners(help.registerCommand(new WhoKnowsSongCommand(dao)))
                .addEventListeners(help.registerCommand(new CrownsStolenCommand(dao)))
                .addEventListeners(help.registerCommand(new FavesFromArtistCommand(dao)))
                .addEventListeners(help.registerCommand(new AlbumCrownsCommand(dao)))
                .addEventListeners(help.registerCommand(new AlbumCrownsLeaderboardCommand(dao)))
                .addEventListeners(help.registerCommand(new PrefixCommand(dao)))
                .addEventListeners(help.registerCommand(new DailyCommand(dao)))
                .addEventListeners(help.registerCommand(new WeeklyCommand(dao)))
                .addEventListeners(help.registerCommand(new UserTopTrackCommand(dao)))
                .addEventListeners(help.registerCommand(new SummaryArtistCommand(dao)))
                .addEventListeners(help.registerCommand(new InviteCommand(dao)))
                .addEventListeners(help.registerCommand(new SourceCommand(dao)))
                .addEventListeners(help.registerCommand(new GlobalArtistCommand(dao)))
                .addEventListeners(help.registerCommand(new GlobalCrownsCommand(dao)))
                .addEventListeners(help.registerCommand(new GlobalUniquesCommand(dao)))
                .addEventListeners(help.registerCommand(new ArtistFrequencyCommand(dao)))
                .addEventListeners(help.registerCommand(new GlobalArtistFrequenciesCommand(dao)))
                .addEventListeners(help.registerCommand(new ArtistFromCountryCommand(dao)))
                .addEventListeners(help.registerCommand(new AlbumInfoCommand(dao)))
                .addEventListeners(help.registerCommand(new TrackInfoCommand(dao)))
                .addEventListeners(help.registerCommand(new TrackPlaysCommand(dao)))
                .addEventListeners(help.registerCommand(new MatchingArtistCommand(dao)))
                .addEventListeners(help.registerCommand(new GlobalTotalArtistPlaysCountCommand(dao)))
                .addEventListeners(help.registerCommand(new TotalArtistPlayCountCommand(dao)))
                .addEventListeners(help.registerCommand(new AliasCommand(dao)))
                .addEventListeners(help.registerCommand(new PaceCommand(dao)))
                .addEventListeners(help.registerCommand(new StreakCommand(dao)))
                .addEventListeners(help.registerCommand(new AliasReviewCommand(dao)))
                .addEventListeners(help.registerCommand(new UserExportCommand(dao)))
                .addEventListeners(help.registerCommand(new ImportCommand(dao)))
                .addEventListeners(help.registerCommand(new VotingCommand(dao)))
                .addEventListeners(help.registerCommand(new AliasesCommand(dao)))
                .addEventListeners(help.registerCommand(new SupportCommand(dao)))
                .addEventListeners(help.registerCommand(new WastedChartCommand(dao)))
                .addEventListeners(help.registerCommand(new WastedAlbumChartCommand(dao)))
                .addEventListeners(help.registerCommand(new WastedTrackCommand(dao)))
                .addEventListeners(help.registerCommand(new ReportReviewCommand(dao)))
                .addEventListeners(help.registerCommand(new GuildArtistPlaysCommand(dao)))
                .addEventListeners(help.registerCommand(new ScrobblesSinceCommand(dao)))
                .addEventListeners(help.registerCommand(new UserResumeCommand(dao)))
                .addEventListeners(help.registerCommand(new AffinityCommand(dao)))
                .addEventListeners(help.registerCommand(new RecommendationCommand(dao)))
                .addEventListeners(help.registerCommand(new UnsetCommand(dao)))
                .addEventListeners(help.registerCommand(new GuildConfigCommand(dao)))
                .addEventListeners(help.registerCommand(new ScrobblesLbCommand(dao)))
                .addEventListeners(help.registerCommand(new ScrobblesCommand(dao)))
                .addEventListeners(help.registerCommand(new RainbowChartCommand(dao)))
                .addEventListeners(help.registerCommand(new ColorChartCommand(dao)))
                .addEventListeners(help.registerCommand(new CrownableCommand(dao)))
                .addEventListeners(help.registerCommand(new RateLimitCommand(dao)))
                .addEventListeners(help.registerCommand(new AOTDCommand(dao)))
                .addEventListeners(help.registerCommand(new UserConfigCommand(dao)))
                .addEventListeners(help.registerCommand(new DisabledCommand(dao)))
                .addEventListeners(help.registerCommand(new DisabledStatusCommand(dao)))
                .addEventListeners(help.registerCommand(new UrlQueueReview(dao)))
                .addEventListeners(help.registerCommand(new CoverCommand(dao)))
                .addEventListeners(help.registerCommand(new LastFmLinkCommand(dao)))
                .addEventListeners(help.registerCommand(new GenreInfoCommand(dao)))
                .addEventListeners(help.registerCommand(new GenreAlbumsCommands(dao)))
                .addEventListeners(help.registerCommand(new GayCommand(dao)))
                .addEventListeners(help.registerCommand(new GlobalWhoKnowsCommand(dao)))
                .addEventListeners(help.registerCommand(new RYMDumpImportCommand(dao)))
                .addEventListeners(help.registerCommand(new AlbumRatings(dao)))
                .addEventListeners(help.registerCommand(new ArtistRatingsCommand(dao)))
                .addEventListeners(help.registerCommand(new TopRatingsCommand(dao)))
                .addEventListeners(help.registerCommand(new TopServerRatingsCommand(dao)))
                .addEventListeners(help.registerCommand(new UserRatings(dao)))
                .addEventListeners(help.registerCommand(new PrivacySetterCommand(dao)))
                .addEventListeners(help.registerCommand(new GlobalMatchingCommand(dao)))
                .addEventListeners(help.registerCommand(new GlobalAffinity(dao)))
                .addEventListeners(help.registerCommand(new GlobalRecommendationCommand(dao)))
                .addEventListeners(help.registerCommand(new LanguageCommand(dao)))
                .addEventListeners(help.registerCommand(new AlbumRecommendationCommand(dao)))
                .addEventListeners(help.registerCommand(new UnratedAlbums(dao)))
                .addEventListeners(help.registerCommand(new GlobalWhoKnowsAlbumCommand(dao)))
                .addEventListeners(help.registerCommand(new WhoKnowsAlbumCommand(dao)))
                .addEventListeners(help.registerCommand(new BandInfoGlobalCommand(dao)))
                .addEventListeners(help.registerCommand(new BandInfoServerCommand(dao)))
                .addEventListeners(help.registerCommand(new HardwareStatsCommand(dao)))
                .addEventListeners(help.registerCommand(new RYMChartCommand(dao)))
                .addEventListeners(help.registerCommand(new BillboardCommand(dao)))
                .addEventListeners(help.registerCommand(new BillboardAlbumCommand(dao)))
                .addEventListeners(help.registerCommand(new BillboardArtistCommand(dao)))
                .addEventListeners(help.registerCommand(new GlobalBillboardCommand(dao)))
                .addEventListeners(help.registerCommand(new GlobalAlbumBillboardCommand(dao)))
                .addEventListeners(help.registerCommand(new GlobalArtistBillboardCommand(dao)))
                .addEventListeners(help.registerCommand(new MyCombosCommand(dao)))
                .addEventListeners(help.registerCommand(new TopCombosCommand(dao)))
                .addEventListeners(help.registerCommand(new BehindArtistsCommand(dao)))
                .addEventListeners(help.registerCommand(new TopArtistComboCommand(dao)))
                .addEventListeners(help.registerCommand(new PaceArtistCommand(dao)))
                .addEventListeners(help.registerCommand(new RandomLinkRatingCommand(dao)))
                .addEventListeners(help.registerCommand(new TopRatedRandomUrls(dao)))
                .addEventListeners(help.registerCommand(new MyTopRatedRandomUrls(dao)))
                .addEventListeners(help.registerCommand(new GuildTopAlbumsCommand(dao)))
                .addEventListeners(help.registerCommand(new ServerAOTY(dao)))
                .addEventListeners(evalCommand)
                .addEventListeners(help.registerCommand(new ServerBanCommand(dao)))
                .addEventListeners(help.registerCommand(new DiscoveredAlbumCommand(dao)))
                .addEventListeners(help.registerCommand(new DiscoveredArtistCommand(dao)))
                .addEventListeners(help.registerCommand(new DiscoveredRatioCommand(dao)))
                .addEventListeners(help.registerCommand(new DiscoveredAlbumRatioCommand(dao)))


                .addEventListeners(new AwaitReady(counter, (ShardManager shard) -> {
                    initDisabledCommands(dao, shard);
                    initPrivateLastfms(dao);
                    prefixCommand.onStartup(shard);
                    if (!isTest) {
                        commandAdministrator.onStartup(shardManager);
                    }
                    evalCommand.setOwnerId(shard);

                    shardManager.addEventListener(help.registerCommand(new FeaturedCommand(dao, scheduledExecutorService)));
                    updatePresence("Chuu");
                }));


        try {

            shardManager = builder.build();

            scheduledExecutorService.scheduleAtFixedRate(
                    new UpdaterThread(dao, true), 0, 120,
                    TimeUnit.SECONDS);

            if (!isTest) {
                scheduledExecutorService.scheduleAtFixedRate(new ImageUpdaterThread(dao), 20, 12, TimeUnit.MINUTES);
                scheduledExecutorService.scheduleAtFixedRate(
                        new SpotifyUpdaterThread(dao), 20, 21,
                        TimeUnit.MINUTES);
            }
            scheduledExecutorService.scheduleAtFixedRate(new ArtistMbidUpdater(dao), 1, 2, TimeUnit.MINUTES);

        } catch (LoginException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
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

    private static void initDisabledCommands(ChuuService dao, ShardManager jda) {
        Map<String, MyCommand<?>> commandsByName = jda.getShards().get(0).getRegisteredListeners().stream().filter(x -> x instanceof MyCommand<?>).map(x -> (MyCommand<?>) x).collect(Collectors.toMap(MyCommand::getName, x -> x));
        MultiValuedMap<Long, String> serverDisables = dao.initServerCommandStatuses();
        serverDisables.entries().forEach(x -> Chuu.disabledServersMap.put(x.getKey(), commandsByName.get(x.getValue())));
        MultiValuedMap<Pair<Long, Long>, String> channelDisables = dao.initServerChannelsCommandStatuses(false);
        channelDisables.entries().forEach(x -> Chuu.disabledChannelsMap.put(x.getKey(), commandsByName.get(x.getValue())));
        MultiValuedMap<Pair<Long, Long>, String> channelEnables = dao.initServerChannelsCommandStatuses(true);
        channelEnables.entries().forEach(x -> Chuu.enabledChannelsMap.put(x.getKey(), commandsByName.get(x.getValue())));

    }

    public static Logger getLogger() {
        return logger;
    }

    private static Map<Long, Character> initPrefixMap(ChuuService dao) {
        return (dao.getGuildPrefixes());
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
