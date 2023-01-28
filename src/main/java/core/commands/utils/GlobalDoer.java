package core.commands.utils;

import core.commands.Context;
import dao.ChuuService;
import dao.entities.GlobalCrown;
import dao.entities.LastFMData;
import dao.entities.PrivacyMode;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.LongSupplier;

public record GlobalDoer(ChuuService db, List<GlobalCrown> globals) {
    public EmbedBuilder generate(long userId, Context e, String str, String url, LongSupplier serverPlays, LongSupplier serverListers, @Nullable String authorImg, @Nullable String authorLink) {
        Optional<GlobalCrown> yourPosition = globals.stream().filter(x -> x.discordId() == userId).findFirst();
        int totalPeople = globals.size();
        int totalPlays = globals.stream().mapToInt(GlobalCrown::playcount).sum();
        EmbedBuilder embedBuilder = new ChuuEmbedBuilder(e);

        if (yourPosition.isPresent()) {
            GlobalCrown globalCrown = yourPosition.get();
            int position = globalCrown.ranking();

            embedBuilder.addField("Position:", position + "/" + totalPeople, true);
            //It means we have someone ahead of us
            if (position != 1) {
                try {
                    LastFMData lastFMData = db.findLastFMData(globals.get(0).discordId());
                    if (EnumSet.of(PrivacyMode.LAST_NAME, PrivacyMode.TAG, PrivacyMode.DISCORD_NAME).contains(lastFMData.getPrivacyMode())) {
                        String embedText = PrivacyUtils.getPublicStr(lastFMData.getPrivacyMode(), lastFMData.getDiscordId(), lastFMData.getName(), e);
                        embedBuilder.addField("Crown Holder: ", embedText, true);
                    }
                } catch (InstanceNotFoundException ignored) {
                    // Do Nothing
                }
                if (position == 2) {
                    if (globals.get(0).bootedAccount()) {
                        embedBuilder.addField("Plays for global crown:", String.valueOf((globals.get(0).playcount() - globalCrown.playcount() + 1)), true);
                    }
                    embedBuilder.addField("Plays for global crown:", String.valueOf((globals.get(0).playcount() - globalCrown.playcount() + 1)), true)
                            .addField("Your Plays:", String.valueOf(globalCrown.playcount()), true);

                } else {
                    embedBuilder.addField("Plays to rank up:", String.valueOf((globals.get(position - 2).playcount() - globalCrown.playcount() + 1)), true)
                            .addField("Plays for first position:", String.valueOf((globals.get(0).playcount() - globalCrown.playcount() + 1)), true)
                            .addField("Your Plays:", String.valueOf(globalCrown.playcount()), false);
                }

            } else {
                if (globals.size() > 1) {
                    embedBuilder.addField("Ahead of second:", (globalCrown.playcount() - globals.get(1).playcount()) + " plays", true);
                } else {
                    embedBuilder.addBlankField(true);
                }
                embedBuilder.addField("Your Plays:", String.valueOf(globalCrown.playcount()), true);
            }
        } else {
            embedBuilder.addField("Plays for first position:", String.valueOf((globals.get(0).playcount())), false);
        }
        if (e.isFromGuild()) {
            StringBuilder serverStats = new StringBuilder();
            long artistFrequencies = serverListers.getAsLong();
            serverStats.append(String.format("**%d** listeners%n", artistFrequencies));
            long serverArtistPlays = serverPlays.getAsLong();
            serverStats.append(String.format("**%d** plays%n", serverArtistPlays));
            embedBuilder.
                    addField(String.format("%s's stats", CommandUtil.escapeMarkdown(e.getGuild().getName())), serverStats.toString(), true);
        }

        String globalStats = String.format("**%d** listeners%n", totalPeople) +
                String.format("**%d** plays%n", totalPlays);
        embedBuilder
                .addField(String.format("%s's stats", CommandUtil.escapeMarkdown(e.getJDA().getSelfUser().getName())), globalStats, true)
                .setImage(url)
                .setAuthor(str, authorLink, authorImg);
        return embedBuilder;
    }

}

