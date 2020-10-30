package core.commands;

import dao.entities.NPMode;

import java.util.EnumSet;

public class NPModeParams {
    private final EnumSet<NPMode> modes;
    private final boolean isHelp;
    private final boolean isListing;

    public NPModeParams(EnumSet<NPMode> modes, boolean isHelp, boolean isListing) {
        this.modes = modes;
        this.isHelp = isHelp;
        this.isListing = isListing;
    }

    public EnumSet<NPMode> getModes() {
        return modes;
    }

    public boolean isHelp() {
        return isHelp;
    }

    public boolean isListing() {
        return isListing;
    }
}
