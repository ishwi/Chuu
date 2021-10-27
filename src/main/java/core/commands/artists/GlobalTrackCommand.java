package core.commands.artists;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.GlobalDoer;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.parsers.ArtistSongParser;
import core.parsers.Parser;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.utils.Optionals;
import core.services.validators.ArtistValidator;
import core.services.validators.TrackValidator;
import dao.ServiceView;
import dao.entities.GlobalCrown;
import dao.entities.ScrobbledArtist;
import dao.entities.ScrobbledTrack;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import java.util.List;

public class GlobalTrackCommand extends ConcurrentCommand<ArtistAlbumParameters> {

    public GlobalTrackCommand(ServiceView dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.BOT_STATS;
    }

    @Override
    public Parser<ArtistAlbumParameters> initParser() {
        var parser = new ArtistSongParser(db, lastFM);
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
        return List.of("globaltrack", "gtr");
    }

    @Override
    public String slashName() {
        return "song-rank";
    }

    @Override
    public String getName() {
        return "Global song overview";
    }

    @Override
    public void onCommand(Context e, @Nonnull ArtistAlbumParameters params) throws LastFmException {

        long userId = params.getLastFMData().getDiscordId();
        ScrobbledArtist sA = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), true, !params.isNoredirect());

        ScrobbledTrack sT = new TrackValidator(db, lastFM).validate(sA.getArtistId(), params.getArtist(), params.getAlbum());

        boolean showBotted = CommandUtil.showBottedAccounts(params.getLastFMData(), params, db);

        List<GlobalCrown> globalArtistRanking = db.getGlobalTrackRanking(sT.getTrackId(), showBotted, e.getAuthor().getIdLong());
        if (globalArtistRanking.isEmpty()) {
            String str = "No one knows **%s** by **%s**".formatted(CommandUtil.escapeMarkdown(sT.getName()), CommandUtil.escapeMarkdown(sA.getArtist()));
            sendMessageQueue(e, str);
            return;
        }

        String str = CommandUtil.escapeMarkdown("%s by %s".formatted(sT.getName(), sA.getArtist()));


        String linkLFM = PrivacyUtils.getLastFmArtistTrackUserUrl(sA.getArtist(), sT.getName(), params.getLastFMData().getName());
        String cover = Chuu.getCoverService().getCover(sT.getAlbumId(), sT.getImageUrl(), e);

        EmbedBuilder eb = new GlobalDoer(db, globalArtistRanking).generate(userId, e, str, cover,
                () -> db.getServerTrackPlays(e.getGuild().getIdLong(), sT.getTrackId()),
                () -> db.getSongFrequencies(e.getGuild().getIdLong(), sT.getTrackId()),
                sA.getUrl(), linkLFM);

        e.sendMessage(eb.build()).queue();

    }

}
