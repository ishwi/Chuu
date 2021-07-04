package core.commands.artists;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.GlobalDoer;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.parsers.ArtistAlbumParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.utils.Optionals;
import core.services.CoverService;
import core.services.validators.AlbumValidator;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.GlobalCrown;
import dao.entities.ScrobbledAlbum;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;

public class GlobalAlbumCommand extends ConcurrentCommand<ArtistAlbumParameters> {

    public GlobalAlbumCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_STATS;
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        var parser = new ArtistAlbumParser(db, lastFM);
        parser.addOptional(Optionals.NOBOTTED.opt);
        parser.addOptional(Optionals.BOTTED.opt);
        return parser;
    }

    @Override
    public String getDescription() {
        return "An overview of your global ranking of an album";
    }

    @Override
    public List<String> getAliases() {
        return List.of("globalalbum", "galb");
    }

    @Override
    public String slashName() {
        return "album-rank";
    }

    @Override
    public String getName() {
        return "Global Album Overview";
    }

    @Override
    protected void onCommand(Context e, @NotNull ArtistAlbumParameters params) throws LastFmException {

        long userId = params.getLastFMData().getDiscordId();
        ScrobbledArtist sA = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), true, !params.isNoredirect());
        ScrobbledAlbum sAlb = new AlbumValidator(db, lastFM).validate(sA.getArtistId(), sA.getArtist(), params.getAlbum());

        boolean showBotted = CommandUtil.showBottedAccounts(params.getLastFMData(), params, db);

        List<GlobalCrown> globalArtistRanking = db.getGlobalAlbumRanking(sAlb.getAlbumId(), showBotted, e.getAuthor().getIdLong());
        if (globalArtistRanking.isEmpty()) {
            sendMessageQueue(e, "No one knows **%s** by **%s**".formatted(CommandUtil.escapeMarkdown(sAlb.getAlbum()), CommandUtil.escapeMarkdown(sA.getArtist())));
            return;
        }

        String str = CommandUtil.escapeMarkdown("%s by %s".formatted(sAlb.getAlbum(), sA.getArtist()));

        String linkLFM = PrivacyUtils.getLastFmAlbumUserUrl(sA.getArtist(), sAlb.getAlbum(), params.getLastFMData().getName());
        String cover = new CoverService(db).getCover(sAlb.getAlbumId(), sAlb.getUrl(), e);

        EmbedBuilder eb = new GlobalDoer(db, globalArtistRanking).generate(userId, e, str, cover,
                () -> db.getServerAlbumPlays(e.getGuild().getIdLong(), sAlb.getAlbumId()),
                () -> db.getAlbumFrequencies(e.getGuild().getIdLong(), sAlb.getAlbumId()),

                sA.getUrl(), linkLFM);

        e.sendMessage(eb.build()).queue();

    }

}
