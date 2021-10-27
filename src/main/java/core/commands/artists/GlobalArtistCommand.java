package core.commands.artists;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.GlobalDoer;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.parsers.utils.Optionals;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.DiscordUserDisplay;
import dao.entities.GlobalCrown;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.util.List;

public class GlobalArtistCommand extends ConcurrentCommand<ArtistParameters> {

    public GlobalArtistCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_STATS;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        ArtistParser parser = new ArtistParser(db, lastFM);
        parser.addOptional(Optionals.NOBOTTED.opt);
        parser.addOptional(Optionals.BOTTED.opt);
        return parser;
    }

    @Override
    public String getDescription() {
        return "An overview of your global ranking of an artist";
    }

    @Override
    public List<String> getAliases() {
        return List.of("global", "g");
    }

    @Override
    public String slashName() {
        return "rank";
    }

    @Override
    public String getName() {
        return "Global artist overview";
    }

    @Override
    public void onCommand(Context e, @Nonnull ArtistParameters params) throws LastFmException {

        long userId = params.getLastFMData().getDiscordId();
        ScrobbledArtist sA = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), true, !params.isNoredirect());

        boolean showBotted = CommandUtil.showBottedAccounts(params.getLastFMData(), params, db);

        List<GlobalCrown> globalArtistRanking = db.getGlobalArtistRanking(sA.getArtistId(), showBotted, e.getAuthor().getIdLong());
        String artist = CommandUtil.escapeMarkdown(sA.getArtist());
        if (globalArtistRanking.isEmpty()) {
            sendMessageQueue(e, "No one knows " + artist);
            return;
        }

        DiscordUserDisplay uInfo = CommandUtil.getUserInfoUnescaped(e, userId);
        EmbedBuilder eb = new GlobalDoer(db, globalArtistRanking).
                generate(userId, e, artist, sA.getUrl(),
                        () -> db.getServerArtistPlays(e.getGuild().getIdLong(), sA.getArtistId()),
                        () -> db.getArtistFrequencies(e.getGuild().getIdLong(), sA.getArtistId()),
                        uInfo.urlImage(),
                        PrivacyUtils.getLastFmArtistUserUrl(sA.getArtist(), params.getLastFMData().getName()));
        e.sendMessage(eb.build()).queue();

    }

}
