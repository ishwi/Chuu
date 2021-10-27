package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.commands.utils.PrivacyUtils;
import core.exceptions.LastFmException;
import core.otherlisteners.util.PaginatorBuilder;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.ScrobbledArtist;
import dao.entities.UserListened;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.TimeFormat;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

public class WhoLastCommand extends ConcurrentCommand<ArtistParameters> {

    public WhoLastCommand(ServiceView dao) {
        super(dao, true);
        respondInPrivate = false;
    }

    public static void handleUserListened(Context e, ArtistParameters params, List<UserListened> lastListeners, boolean isFirst) {


        String usable = CommandUtil.escapeMarkdown(e.getGuild().getName());

        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e)
                .setTitle("Who listened " + (isFirst ? "first" : "last") + " to " + params.getScrobbledArtist().getArtist() + " in " + usable)
                .setThumbnail(CommandUtil.noImageUrl(params.getScrobbledArtist().getUrl()))
                .setFooter(lastListeners.size() + CommandUtil.singlePlural(lastListeners.size(), " listener", " listeners"));

        Function<UserListened, String> toMemoize = (userListened) -> {
            String whom;
            if (userListened.moment().isEmpty()) {
                whom = "**Never**";
            } else {
                whom = "**" + CommandUtil.getDateTimestampt(userListened.moment().get(), TimeFormat.RELATIVE) + "**";
            }
            return ". [" + CommandUtil.getUserInfoEscaped(e, userListened.discordId()).username() + "](" + PrivacyUtils.getLastFmUser(userListened.lastfmId()) + "): " + whom + "\n";
        };

        new PaginatorBuilder<>(e, embedBuilder, lastListeners).memoized(toMemoize).build().queue();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.SERVER_LEADERBOARDS;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(db, lastFM);
    }

    @Override
    public String getDescription() {
        return "Who listened last to an artist on a server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("wholast", "wl", "wlast", "whol");
    }

    @Override
    public String getName() {
        return "Who listened last";
    }

    @Override
    public void onCommand(Context e, @Nonnull ArtistParameters params) throws LastFmException {

        ScrobbledArtist sA = new ArtistValidator(db, lastFM, e).validate(params.getArtist(), !params.isNoredirect());
        params.setScrobbledArtist(sA);

        List<UserListened> lasts = db.getServerLastScrobbledArtist(sA.getArtistId(), e.getGuild().getIdLong());
        if (lasts.isEmpty()) {
            sendMessageQueue(e, "Couldn't get the last time this server scrobbled **" + sA.getArtist() + "**");
            return;
        }

        handleUserListened(e, params, lasts, false);

    }

}
