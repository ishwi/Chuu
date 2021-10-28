package core.commands.utils;

import core.Chuu;
import core.apis.last.entities.chartentities.AlbumChart;
import core.apis.last.entities.chartentities.ArtistChart;
import core.apis.last.entities.chartentities.TrackChart;
import core.apis.last.entities.chartentities.UrlCapsule;
import core.commands.Context;
import core.commands.artists.BandInfoCommand;
import core.commands.artists.BandInfoServerCommand;
import core.commands.charts.GuildTopAlbumsCommand;
import core.commands.charts.GuildTopCommand;
import core.commands.charts.GuildTopTracksCommand;
import core.commands.whoknows.LocalWhoKnowsAlbumCommand;
import core.commands.whoknows.LocalWhoKnowsSongCommand;
import core.commands.whoknows.WhoKnowsCommand;
import core.exceptions.LastFmException;
import core.parsers.params.ArtistAlbumParameters;
import core.parsers.params.ArtistParameters;
import core.parsers.params.ChartSizeParameters;
import core.services.validators.ArtistValidator;
import dao.ServiceView;
import dao.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import org.knowm.xchart.PieChart;

import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public record FriendCommandLoader(WhoKnowsCommand whoKnowsCommand,
                                  LocalWhoKnowsSongCommand localWhoKnowsSongCommand,
                                  LocalWhoKnowsAlbumCommand localWhoKnowsAlbumCommand,
                                  GuildTopCommand guildTopCommand,
                                  GuildTopAlbumsCommand guildTopAlbumsCommand,
                                  GuildTopTracksCommand guildTopTracksCommand,
                                  BandInfoCommand bandInfoCommand
) {
    public FriendCommandLoader(ServiceView dao) {
        this(initFriendsWhoknows(dao), initFriendsWhoknowsSong(dao), initFriendsWhoknowsAlbum(dao), initChartCommand(dao), initGuildTopAlbumsCommand(dao), guildTopTracksCommand(dao), artistCommand(dao));
    }

    private static WhoKnowsCommand initFriendsWhoknows(ServiceView dao) {
        return new WhoKnowsCommand(dao) {
            @Override
            protected WrapperReturnNowPlaying generateWrapper(ArtistParameters params, WhoKnowsMode whoKnowsMode) throws LastFmException {
                ScrobbledArtist sA = new ArtistValidator(db, lastFM, params.getE()).validate(params.getArtist(), !params.isNoredirect());
                params.setScrobbledArtist(sA);
                long author = params.getLastFMData().getDiscordId();
                int limit = whoKnowsMode.equals(WhoKnowsMode.IMAGE) ? 10 : Integer.MAX_VALUE;
                return db.friendsWhoKnows(sA.getArtistId(), author, limit);
            }

            @Override
            protected String getImageTitle(Context e, ArtistParameters params) {
                DiscordUserDisplay ui = CommandUtil.getUserInfoUnescaped(e, params.getLastFMData().getDiscordId());
                return "%s's friendlist".formatted(ui.username());
            }

            @Override
            public String getTitle(ArtistParameters params, String baseTitle) {
                DiscordUserDisplay ui = CommandUtil.getUserInfoUnescaped(params.getE(), params.getLastFMData().getDiscordId());
                return "Who knows " + CommandUtil.escapeMarkdown(params.getScrobbledArtist().getArtist()) + " in %s's friendlist?".formatted(ui.username());
            }
        };
    }

    private static LocalWhoKnowsSongCommand initFriendsWhoknowsSong(ServiceView dao) {
        return new LocalWhoKnowsSongCommand(dao) {
            protected WrapperReturnNowPlaying generateInnerWrapper(ArtistAlbumParameters ap, WhoKnowsMode effectiveMode, long trackId) {
                long discordId = ap.getLastFMData().getDiscordId();
                return effectiveMode.equals(WhoKnowsMode.IMAGE) ?
                        this.db.friendsWhoKnowsSong(trackId, discordId, 10) :
                        this.db.friendsWhoKnowsSong(trackId, discordId, Integer.MAX_VALUE);
            }

            @Override
            protected String getImageTitle(Context e, ArtistAlbumParameters params) {
                DiscordUserDisplay ui = CommandUtil.getUserInfoUnescaped(e, params.getLastFMData().getDiscordId());
                return "%s's friendlist".formatted(ui.username());
            }

            @Override
            public String getTitle(ArtistAlbumParameters params, String baseTitle) {
                DiscordUserDisplay ui = CommandUtil.getUserInfoUnescaped(params.getE(), params.getLastFMData().getDiscordId());
                return "Who knows " + CommandUtil.escapeMarkdown(params.getArtist() + " - " + params.getAlbum()) + " in %s's friendlist?".formatted(ui.username());
            }
        };
    }

    private static LocalWhoKnowsAlbumCommand initFriendsWhoknowsAlbum(ServiceView dao) {
        return new LocalWhoKnowsAlbumCommand(dao) {

            protected WrapperReturnNowPlaying generateInnerWrapper(ArtistAlbumParameters ap, WhoKnowsMode effectiveMode, long albumId) {

                long disc = ap.getLastFMData().getDiscordId();
                return effectiveMode.equals(WhoKnowsMode.IMAGE) ?
                        this.db.friendsWhoKnowsAlbum(albumId, disc, 10) :
                        this.db.friendsWhoKnowsAlbum(albumId, disc, Integer.MAX_VALUE);
            }

            @Override
            protected String getImageTitle(Context e, ArtistAlbumParameters params) {
                DiscordUserDisplay ui = CommandUtil.getUserInfoUnescaped(e, params.getLastFMData().getDiscordId());
                return "%s's friendlist".formatted(ui.username());
            }

            @Override
            public String getTitle(ArtistAlbumParameters params, String baseTitle) {
                DiscordUserDisplay ui = CommandUtil.getUserInfoUnescaped(params.getE(), params.getLastFMData().getDiscordId());
                return "Who knows " + CommandUtil.escapeMarkdown(params.getArtist() + " - " + params.getAlbum()) + " in %s's friendlist?".formatted(ui.username());
            }
        };


    }

    private static GuildTopCommand initChartCommand(ServiceView dao) {
        return new GuildTopCommand(dao) {
            @Override
            public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartSizeParameters gp) {
                ChartMode effectiveMode = getEffectiveMode(gp);
                ResultWrapper<ScrobbledArtist> guildTop = db.friendsTopArtists(gp.getDiscordId(),
                        gp.getX() * gp.getY(),
                        !(effectiveMode.equals(ChartMode.IMAGE) && gp.chartMode().equals(ChartMode.IMAGE) || gp.chartMode().equals(ChartMode.IMAGE_ASIDE)));

                AtomicInteger counter = new AtomicInteger(0);
                BlockingQueue<UrlCapsule> guildTopQ = guildTop.getResultList().stream().sorted(Comparator.comparingInt(ScrobbledArtist::getCount).reversed()).
                        map(x ->
                                new ArtistChart(x.getUrl(), counter.getAndIncrement(), x.getArtist(), null, x.getCount(), gp.isWriteTitles(), gp.isWritePlays(), gp.isAside())
                        ).collect(Collectors.toCollection(LinkedBlockingDeque::new));
                return new CountWrapper<>(guildTop.getRows(), guildTopQ);
            }

            @Override
            public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartSizeParameters params, int count) {
                long discordId = params.getDiscordId();
                DiscordUserDisplay ui = CommandUtil.getUserInfoEscaped(params.getE(), discordId);
                String titleInit = " and friends top artists";
                String footerText = " and their friends have listened to " + count + " artists";
                return embedBuilder.setAuthor(ui.username() + titleInit,
                                null, params.getE().getGuild().getIconUrl())
                        .setFooter(CommandUtil.stripEscapedMarkdown(ui.username()) + footerText);
            }

            @Override
            public void doPie(PieChart pieChart, ChartSizeParameters gp, int count) {
                DiscordUserDisplay ui = CommandUtil.getUserInfoEscaped(gp.getE(), gp.getDiscordId());

                String subtitle = String.format("%s and their friends have listened to %d artists (showing top %d)", ui.username(), count, gp.getX() * gp.getY());
                pieChart.setTitle(ui.username() + " and friends top artists");
                String urlImage = gp.getE().getJDA().getSelfUser().getAvatarUrl();

                sendImage(new PieDoer(subtitle, urlImage, pieChart).fill(), gp.getE());
            }
        };
    }

    private static GuildTopAlbumsCommand initGuildTopAlbumsCommand(ServiceView dao) {
        return new GuildTopAlbumsCommand(dao) {
            @Override
            public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartSizeParameters gp) {
                ChartMode effectiveMode = getEffectiveMode(gp);
                ResultWrapper<ScrobbledAlbum> guildTop = db.friendsTopAlbums(gp.getDiscordId(),
                        gp.getX() * gp.getY(),
                        !(effectiveMode.equals(ChartMode.IMAGE) && gp.chartMode().equals(ChartMode.IMAGE) || gp.chartMode().equals(ChartMode.IMAGE_ASIDE)));

                AtomicInteger counter = new AtomicInteger(0);
                BlockingQueue<UrlCapsule> guildTopQ = guildTop.getResultList().stream().sorted(Comparator.comparingInt(ScrobbledAlbum::getCount).reversed()).
                        map(x ->
                                new AlbumChart(x.getUrl(), counter.getAndIncrement(), x.getAlbum(), x.getArtist(), null, x.getCount(), gp.isWriteTitles(), gp.isWritePlays(), gp.isAside())
                        ).collect(Collectors.toCollection(LinkedBlockingDeque::new));
                return new CountWrapper<>(guildTop.getRows(), guildTopQ);
            }

            @Override
            public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartSizeParameters params, int count) {
                long discordId = params.getDiscordId();
                DiscordUserDisplay ui = CommandUtil.getUserInfoEscaped(params.getE(), discordId);
                String titleInit = " and friends top albums";
                String footerText = " and their friends have listened to " + count + " albums";
                return embedBuilder.setAuthor(ui.username() + titleInit,
                                null, params.getE().getGuild().getIconUrl())
                        .setFooter(CommandUtil.stripEscapedMarkdown(ui.username()) + footerText);
            }

            @Override
            public void doPie(PieChart pieChart, ChartSizeParameters gp, int count) {
                DiscordUserDisplay ui = CommandUtil.getUserInfoEscaped(gp.getE(), gp.getDiscordId());

                String subtitle = String.format("%s and their friends have listened to %d albums (showing top %d)", ui.username(), count, gp.getX() * gp.getY());
                pieChart.setTitle(ui.username() + " and friends top albums");
                String urlImage = gp.getE().getJDA().getSelfUser().getAvatarUrl();

                sendImage(new PieDoer(subtitle, urlImage, pieChart).fill(), gp.getE());
            }
        };
    }

    private static GuildTopTracksCommand guildTopTracksCommand(ServiceView dao) {
        return new GuildTopTracksCommand(dao) {
            @Override
            public CountWrapper<BlockingQueue<UrlCapsule>> processQueue(ChartSizeParameters gp) {
                ChartMode effectiveMode = getEffectiveMode(gp);
                ResultWrapper<ScrobbledTrack> guildTop = db.friendsTopTracks(gp.getDiscordId(),
                        gp.getX() * gp.getY(),
                        !(effectiveMode.equals(ChartMode.IMAGE) && gp.chartMode().equals(ChartMode.IMAGE) || gp.chartMode().equals(ChartMode.IMAGE_ASIDE)));

                AtomicInteger counter = new AtomicInteger(0);
                BlockingQueue<UrlCapsule> guildTopQ = guildTop.getResultList().stream().sorted(Comparator.comparingInt(ScrobbledTrack::getCount).reversed()).
                        map(x ->
                                new TrackChart(x.getImageUrl(), counter.getAndIncrement(), x.getName(), x.getArtist(), null, x.getCount(), gp.isWriteTitles(), gp.isWritePlays(), gp.isAside())
                        ).collect(Collectors.toCollection(LinkedBlockingDeque::new));
                return new CountWrapper<>(guildTop.getRows(), guildTopQ);
            }

            @Override
            public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartSizeParameters params, int count) {
                long discordId = params.getDiscordId();
                DiscordUserDisplay ui = CommandUtil.getUserInfoEscaped(params.getE(), discordId);
                String titleInit = " and friends top tracks";
                String footerText = " and their friends have listened to " + count + " tracks";
                return embedBuilder.setAuthor(ui.username() + titleInit,
                                null, ui.urlImage())
                        .setFooter(CommandUtil.stripEscapedMarkdown(ui.username()) + footerText);
            }

            @Override
            public void doPie(PieChart pieChart, ChartSizeParameters gp, int count) {
                DiscordUserDisplay ui = CommandUtil.getUserInfoEscaped(gp.getE(), gp.getDiscordId());

                String subtitle = String.format("%s and their friends have listened to %d tracks (showing top %d)", ui.username(), count, gp.getX() * gp.getY());
                pieChart.setTitle(ui.username() + " and friends top tracks");
                String urlImage = gp.getE().getJDA().getSelfUser().getAvatarUrl();

                sendImage(new PieDoer(subtitle, urlImage, pieChart).fill(), gp.getE());
            }
        };

    }

    private static BandInfoCommand artistCommand(ServiceView dao) {
        return new BandInfoServerCommand(dao) {
            @Override
            protected void bandLogic(ArtistParameters ap) {
                boolean b = ap.hasOptional("list");
                boolean b1 = ap.hasOptional("pie");
                int limit = b || b1 ? Integer.MAX_VALUE : 9;
                ScrobbledArtist who = ap.getScrobbledArtist();
                long threshold = ap.getLastFMData().getArtistThreshold();
                long author = ap.getLastFMData().getDiscordId();
                List<AlbumUserPlays> userTopArtistAlbums = db.friendsArtistAlbums(limit, who.getArtistId(), author);
                Context e = ap.getE();
                userTopArtistAlbums.forEach(t -> t.setAlbumUrl(Chuu.getCoverService().getCover(t.getArtist(), t.getAlbum(), t.getAlbumUrl(), e)));

                ArtistAlbums ai = new ArtistAlbums(who.getArtist(), userTopArtistAlbums);

                if (b) {
                    doList(ap, ai);
                    return;
                }
                WrapperReturnNowPlaying np = db.friendsWhoKnows(who.getArtistId(), author, 5);
                np.getReturnNowPlayings().forEach(element ->
                        element.setDiscordName(CommandUtil.getUserInfoUnescaped(e, element.getDiscordId()).username())
                );
                BufferedImage logo = CommandUtil.getLogo(db, e);
                if (b1) {
                    doPie(ap, np, ai, logo);
                    return;
                }
                long plays = db.friendsServerArtistPlays(author, who.getArtistId());
                doImage(ap, np, ai, Math.toIntExact(plays), logo, threshold);
            }

            @Override
            protected void configEmbedBuilder(EmbedBuilder embedBuilder, ArtistParameters ap, ArtistAlbums ai) {
                DiscordUserDisplay uInfo = CommandUtil.getUserInfoEscaped(ap.getE(), ap.getLastFMData().getDiscordId());
                embedBuilder.setTitle(uInfo.username() + "'s and friends top " + CommandUtil.escapeMarkdown(ai.getArtist()) + " albums");
            }


        };
    }

}
