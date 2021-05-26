package dao.entities;

import dao.utils.LinkUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ObscurityEntry extends LbEntry<Double> {
    public final static NumberFormat average = new DecimalFormat("#0.##");

    public ObscurityEntry(String lastFMId, long discordId, double crowns) {
        super(lastFMId, discordId, crowns);
    }

    @Override
    public String toStringWildcard() {
        return ". [" +
               LinkUtils.cleanMarkdownCharacter(getDiscordName()) +
               "](" + WILDCARD +
               "): " + average.format(getEntryCount()) +
               "% obscure  \n";
    }

}

