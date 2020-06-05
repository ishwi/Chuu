package core.commands;

import core.Chuu;
import core.exceptions.LastFmException;
import core.parsers.NoOpParser;
import core.parsers.Parser;
import core.parsers.params.CommandParameters;
import dao.ChuuService;
import dao.entities.PresenceInfo;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FeaturedCommand extends ConcurrentCommand<CommandParameters> {
    private static final String DEFAULT_URL = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ce/180902_%EC%8A%A4%EC%B9%B4%EC%9D%B4%ED%8E%98%EC%8A%A4%ED%8B%B0%EB%B2%8C_%EC%9D%B4%EB%8B%AC%EC%9D%98_%EC%86%8C%EB%85%80_yyxy.jpg/800px-180902_%EC%8A%A4%EC%B9%B4%EC%9D%B4%ED%8E%98%EC%8A%A4%ED%8B%B0%EB%B2%8C_%EC%9D%B4%EB%8B%AC%EC%9D%98_%EC%86%8C%EB%85%80_yyxy.jpg";
    private static final String DEFAULT_ARTIST = "LOOΠΔ";
    private static final String DEFAULT_USER = "Chuu";
    private static boolean doSeasonal = true;
    private static final List<String> seasonalContent = List.of("Totalitarian Dystopia - Acrania",
            "Murder Junkies - GG Allin",
            "The Murder of Liddle Towers / Police Oppression - Angelic Upstarts",
            "Die for the Government - Anti-Flag",
            "A New Kind of Army - Anti-Flag",
            "Damaged - Black Flag",
            "Body Count - Body Count",
            "Color Barrier / Metro Pigs - Bad Bad Leroy Brown & His Defence Force",
            "Censurados - Censurados",
            "Crack Rock Steady EP - Choking Victim ",
            "Party Music - The Coup",
            "The Crucifucks - The Crucifucks",
            "Holiday in Cambodia / Police Truck - Dead Kennedys",
            "Police State - Dead Prez ",
            "The Dicks Hate the Police / Lifetime Problems / All Night Fever - The Dicks ",
            "Imperfectly - Ani DiFranco",
            "Police Bastard - Doom",
            "Nothing Is Forgotten, Nothing Is Forgiven - The Empire Line",
            "Police - Fuck Up",
            "The Score - Fugees",
            "The South Park Psycho - Ganksta N.I.P",
            "Crooked Officer - Geto Boys",
            "La pégre - Dominique Grange",
            "بس ربحت, خسرت \"When You Have Won, You Have Lost\" - Haram",
            "Friends. Lovers. Favorites. - The HIRS Collective",
            "Forces of Victory  - Linton Kwesi Johnson",
            "I Just Killed A Cop Now I'm Horny - JPEGMAFIA",
            "Police and Thief / Grumbling Dub - Junior Murvin",
            "The Kids -  The Kids",
            "Don't Die - Killer Mike",
            "Kristofferson - Kris Kristofferson",
            "Sound of Da Police / Hip Hop VS Rap - KRS One ",
            "Fuck World Trade - Leföver Crack - ",
            "We Must Become the Pitiless Censors of Ourselves - John Maus ",
            "Millions of Dead Cops - MDC",
            "Mingus Dynasty - The Next Generation",
            "Bitter Youth - NNaked Aggression -",
            "Straight Outta Compton - N.W.A",
            "Rehearsals for Retirement - Phil Ochs",
            "Feel the Darkness - Posion Idea",
            "Wir wollen keine Bullenschweine - Slime",
            "Blue Lights - Jorja Smith",
            "The Kids Will Have Their Say - SS Decontrol",
            "Anonymous - Stray From The Path",
            "Subliminal Criminals - Stray From The Path",
            "The Tokyos - The Tokyos",
            "Super Tight... - Underground Kingz",
            "Ain't a Damn Thang Changed - WC and The Maad Circle",
            "HEAVN - Jamila Woods",
            "Imaginary Life - Worriers",
            "...Is Toxic to Pigs?? - Xylitol",
            "Sound & Fury - Youth Brigade",
            "Raste - Benjamin Zephaniah");

    private PresenceInfo currentPresence;

    public FeaturedCommand(ChuuService dao, ScheduledExecutorService scheduledManager) {
        super(dao);
        currentPresence = new PresenceInfo(DEFAULT_ARTIST, DEFAULT_URL, Long.MAX_VALUE, 1);
        scheduledManager.scheduleAtFixedRate(() -> {
            try {
                PresenceInfo presenceInfo;
                if (doSeasonal) {
                    int i = CommandUtil.rand.nextInt(seasonalContent.size());
                    String s = seasonalContent.get(i);
                    String[] split = s.split("-");
                    int plays = 0;
                    try {
                        plays = lastFM.getArtistSummary(split[1].trim(), "ishwaracoello").getPlaycount();
                    } catch (LastFmException ignored) {

                    }
                    presenceInfo = new PresenceInfo(s.trim(), "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/Fist.svg/1200px-Fist.svg.png", plays, 1);
                    Chuu.updatePresence(s);
                } else {
                    presenceInfo = getService().getRandomArtistWithUrl();
                    Chuu.updatePresence(presenceInfo.getArtist());
                }
                this.currentPresence = presenceInfo;
                Chuu.getLogger()
                        .info("[" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE) + "]\t!Updated Presence");
            } catch (Exception e) {
                Chuu.getLogger().warn(e.getMessage());
            }
        }, 1, 30, TimeUnit.MINUTES);
    }

    @Override
    protected CommandCategory getCategory() {
        return CommandCategory.BOT_INFO;
    }

    @Override
    public Parser<CommandParameters> getParser() {
        return new NoOpParser();
    }

    @Override
    public String getDescription() {
        return "Info about the artist that appears on the bot status";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("featured");
    }

    @Override
    public String getName() {
        return "Featured Artist";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e) {
        if (e.getAuthor().getIdLong() == 240561665396047872L && e.getMessage().getContentRaw().contains("blm")) {
            doSeasonal = !doSeasonal;
            e.getAuthor().openPrivateChannel().flatMap(x -> x.sendMessage("Change to " + doSeasonal)).queue();
        }
        String userString = this.getUserString(e, currentPresence.getDiscordId(), DEFAULT_USER);
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(CommandUtil.randomColor())
                .setThumbnail(CommandUtil.noImageUrl(currentPresence.getUrl()))
                .setTitle(Chuu.getPresence().getJDA().getSelfUser().getName() + "'s Featured Artist:", CommandUtil
                        .getLastFmArtistUrl(currentPresence.getArtist()))
                .addField(doSeasonal ? ":sunglasses:" : "Artist:", CommandUtil.cleanMarkdownCharacter(currentPresence.getArtist()), false)
                .addField("User:", userString, false)
                .addField("Total Artist Plays:", String.valueOf(currentPresence.getSum()), false);

        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setEmbed(embedBuilder.build()).sendTo(e.getChannel()).queue();
    }
}
