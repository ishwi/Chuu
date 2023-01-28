package core.commands.config;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.ChannelParser;
import core.parsers.GuildConfigParser;
import core.parsers.Parser;
import core.parsers.params.GuildConfigParams;
import core.parsers.params.GuildConfigType;
import core.services.ColorService;
import core.services.VoiceAnnounceService;
import core.util.ServiceView;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class GuildConfigCommand extends ConcurrentCommand<GuildConfigParams> {
    public GuildConfigCommand(ServiceView dao) {
        super(dao);
        this.respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<GuildConfigParams> initParser() {
        return new GuildConfigParser(db);
    }

    @Override
    public String getDescription() {
        return "Configuration per server";
    }

    @Override
    public List<String> getAliases() {
        return List.of("serverconfiguration", "serverconfig", "sconfig");
    }

    @Override
    public String getName() {
        return "Server Configuration";
    }

    @Override
    public void onCommand(Context e, @NotNull GuildConfigParams params) throws LastFmException, InstanceNotFoundException {
        GuildConfigParams parse = this.parser.parse(e);

        GuildConfigType config = parse.getConfig();
        String value = parse.getValue();
        boolean cleansing = value.equalsIgnoreCase("clear");
        long guildId = e.getGuild().getIdLong();
        switch (config) {
            case CROWNS_THRESHOLD -> {
                int threshold = Integer.parseInt(value);
                db.updateGuildCrownThreshold(guildId, threshold);
                sendMessageQueue(e, "Successfully updated the crown threshold to " + threshold);
            }
            case CHART_MODE -> {
                ChartMode chartMode;
                if (cleansing) {
                    chartMode = null;
                } else {
                    chartMode = ChartMode.valueOf(value.replace("-", "_").toUpperCase());
                }
                db.setServerChartMode(guildId, chartMode);
                if (cleansing) {
                    sendMessageQueue(e, "Now all charts are back to the default");
                } else {
                    sendMessageQueue(e, "Server chart mode set to: **" + WordUtils.capitalizeFully(chartMode.toString()) + "**");
                }
            }
            case COLOR -> {
                EmbedColor embedColor;
                if (cleansing) {
                    embedColor = null;
                } else {
                    embedColor = EmbedColor.fromString(value);
                    if (embedColor == null || (embedColor.type() == EmbedColor.EmbedColorType.COLOURS && embedColor.colorList().isEmpty())) {
                        sendMessageQueue(e, "Couldn't read any colour :(\nTry with different values.");
                        return;
                    }
                    if (!embedColor.isValid()) {
                        parser.sendError("Too many colours were introduced. Pls reduce your input a bit", e);
                        return;
                    }
                }


                db.setServerColorMode(guildId, embedColor);
                ColorService.handleServerChange(guildId, embedColor);
                String str = embedColor == null ? "Default" : embedColor.toDisplayString();
                sendMessageQueue(e, "Guild color mode set to: **" + WordUtils.capitalizeFully(str) + "**");
            }
            case WHOKNOWS_DISPLAY_MODE -> {
                WhoKnowsDisplayMode whoKnowsDisplayMode;
                if (cleansing) {
                    whoKnowsDisplayMode = null;
                } else {
                    whoKnowsDisplayMode = WhoKnowsDisplayMode.valueOf(value.replace("-", "_").toUpperCase());
                }
                db.setServerWhoknowMode(guildId, whoKnowsDisplayMode);
                if (cleansing) {
                    sendMessageQueue(e, "Now your who knows are back to the default");
                } else {
                    sendMessageQueue(e, "Who Knows mode set to: **" + WordUtils.capitalizeFully(whoKnowsDisplayMode.toString()) + "**");
                }
            }
            case REMAINING_MODE -> {
                RemainingImagesMode remainingImagesMode;
                if (cleansing) {
                    remainingImagesMode = null;
                } else {
                    remainingImagesMode = RemainingImagesMode.valueOf(value.replace("-", "_").toUpperCase());
                }
                db.setRemainingImagesModeServer(guildId, remainingImagesMode);
                if (!cleansing) {
                    sendMessageQueue(e, "The mode of the remaining image commands was set to: **" + WordUtils.capitalizeFully(remainingImagesMode.toString()) + "**");
                } else {
                    sendMessageQueue(e, "The mode of the remaining image commands to the default");
                }
            }
            case ALLOW_NP_REACTIONS -> {
                boolean b = Boolean.parseBoolean(value);
                db.setServerAllowReactions(guildId, b);
                if (b) {
                    sendMessageQueue(e, "Np reactions are now allowed");
                } else {
                    sendMessageQueue(e, "Np reactions are not allowed anymore");
                }
            }
            case OVERRIDE_NP_REACTIONS -> {
                OverrideMode overrideMode = OverrideMode.valueOf(value.trim().replaceAll("\s+|-", "_").toUpperCase());
                db.setServerOverrideReactions(guildId, overrideMode);
                sendMessageQueue(e, "Set the override mode to: " + WordUtils.capitalizeFully(overrideMode.toString().replaceAll("_", " ")));
            }
            case OVERRIDE_COLOR -> {
                OverrideColorMode overrideColorMode = OverrideColorMode.valueOf(value.trim().replaceAll("\s+|-", "_").toUpperCase());
                db.setServerColorOverride(guildId, overrideColorMode);
                sendMessageQueue(e, "Set the override colour mode to: " + WordUtils.capitalizeFully(overrideColorMode.toString().replaceAll("_", " ")));
            }
            case DELETE_MESSAGE -> {
                boolean b = Boolean.parseBoolean(value);
                if (b) {

                    if (!e.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                        sendMessageQueue(e, "Don't have the permission **%s** so can't delete messages :(".formatted(Permission.MESSAGE_MANAGE.getName()));
                    }
                }
                db.setServerDeleteMessage(guildId, b);
                if (b) {
                    Chuu.getMessageDeletionService().addServerToDelete(guildId);
                    sendMessageQueue(e, "The commands will be deleted by the bot.");

                } else {
                    Chuu.getMessageDeletionService().removeServerToDelete(guildId);
                    sendMessageQueue(e, "The commands won't be deleted by the bot.");
                }
            }
            case SHOW_DISABLED_WARNING -> {
                boolean b = Boolean.parseBoolean(value);
                db.setServerShowDisabledWarning(guildId, b);
                Chuu.getMessageDisablingService().setDontRespondOnError(b, guildId);
                if (b) {
                    sendMessageQueue(e, "The bot won't say anything when you run a disabled command");
                } else {
                    sendMessageQueue(e, "You will be notified when you run a disabled command");

                }
            }
            case NP -> {
                String[] split = value.trim().replaceAll("\s+", " ").split("[|,& ]+");
                EnumSet<NPMode> modes = EnumSet.noneOf(NPMode.class);
                for (String mode : split) {
                    if (mode.equalsIgnoreCase("CLEAR")) {
                        modes = EnumSet.of(NPMode.UNKNOWN);
                        break;
                    }
                    NPMode npMode = NPMode.valueOf(mode.replace("-", "_").toUpperCase());
                    modes.add(npMode);
                }
                if (modes.size() > 15) {
                    sendMessageQueue(e, "You can't set more than 15 as a default for the server");
                } else {
                    db.setServerNPModes(guildId, modes);
                    String strModes = NPMode.getListedName(modes);

                    if (modes.contains(NPMode.UNKNOWN) && modes.size() == 1 || modes.isEmpty()) {
                        sendMessageQueue(e, "Successfully cleared the server config");
                    } else {
                        sendMessageQueue(e, String.format("Successfully changed the server config to the following %s: %s", CommandUtil.singlePlural(modes.size(), "mode", "modes"), strModes));
                    }
                }
            }
            case VOICE_ANNOUNCEMENT_CHANNEL -> {
                if (value.equalsIgnoreCase("clear")) {
                    VoiceAnnounceService vaService = new VoiceAnnounceService(db);
                    VoiceAnnouncement va = vaService.getVoiceAnnouncement(guildId);
                    vaService.setVoiceAnnouncement(guildId, null, va.enabled());
                    if (va.enabled()) {
                        sendMessageQueue(e, "Have cleared the voice announcement channel.");
                    } else {
                        sendMessageQueue(e, "Have cleared the voice announcement channel but I still have announcements disabled!");
                    }
                } else {



                    Optional<GuildChannel> guildChannel = ChannelParser.parseChannel(value, e.getGuild());
                    if (guildChannel.isEmpty()) {
                        sendMessageQueue(e, "Couldn't find any channel with your input!");
                        return;
                    }
                    GuildChannel channel = guildChannel.get();

                    if (channel.getType() != ChannelType.TEXT) {
                        sendMessageQueue(e, "The provided channel needs to be a text channel!");
                        return;
                    }
                    VoiceAnnounceService vaService = new VoiceAnnounceService(db);
                    VoiceAnnouncement va = vaService.getVoiceAnnouncement(guildId);
                    vaService.setVoiceAnnouncement(guildId, channel.getIdLong(), va.enabled());
                    if (va.enabled()) {
                        sendMessageQueue(e, "Have set <#" + channel.getId() + "> as the voice announcement channel.");
                    } else {
                        sendMessageQueue(e, "Have set <#" + channel.getId() + "> as the voice announcement channel but I have announcements disabled!");
                    }
                }
            }
            case VOICE_ANNOUNCEMENT_ENABLED -> {
                boolean enabled = Boolean.parseBoolean(value);
                VoiceAnnounceService vaService = new VoiceAnnounceService(db);
                VoiceAnnouncement va = vaService.getVoiceAnnouncement(guildId);
                vaService.setVoiceAnnouncement(guildId, va.channelId(), enabled);
                if (enabled) {
                    sendMessageQueue(e, "Will announce when a track starts");
                } else {
                    Chuu.getCoverService().removeServer(guildId);

                    sendMessageQueue(e, "Won't announce when a track starts");

                }
            }

            case SET_ON_JOIN -> {
                boolean b = Boolean.parseBoolean(value);
                db.setSetOnJoin(guildId, b);
                if (b) {
                    sendMessageQueue(e, "The bot will auto add known members when they join your server");
                } else {
                    sendMessageQueue(e, "The bot won't auto add known members when they join your server");
                }
            }
            case CENSOR_CONVERS -> {
                boolean allowCovers = !Boolean.parseBoolean(value);
                db.setServerAllowCovers(guildId, allowCovers);

                if (allowCovers) {
                    Chuu.getCoverService().addServer(guildId);

                    sendMessageQueue(e, "NSFW covers won't be censored");
                } else {
                    Chuu.getCoverService().removeServer(guildId);

                    sendMessageQueue(e, "NSFW covers will be censored");
                }
            }
        }
    }
}
