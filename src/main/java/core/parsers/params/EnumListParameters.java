package core.parsers.params;


import core.commands.Context;

import java.util.EnumSet;

public class EnumListParameters<T extends Enum<T>> extends CommandParameters {

    private final EnumSet<T> enums;
    private final boolean isHelp;
    private final boolean isListing;

    public EnumListParameters(Context e, EnumSet<T> enums, boolean isHelp, boolean isListing) {
        super(e);
        this.enums = enums;
        this.isHelp = isHelp;
        this.isListing = isListing;
    }

    public EnumSet<T> getEnums() {
        return enums;
    }

    public boolean isHelp() {
        return isHelp;
    }

    public boolean isListing() {
        return isListing;
    }
}
