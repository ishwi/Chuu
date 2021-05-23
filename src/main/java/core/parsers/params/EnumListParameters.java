package core.parsers.params;


import core.commands.Context;
import net.dv8tion.jda.api.entities.User;

import java.util.EnumSet;

public class EnumListParameters<T extends Enum<T>> extends CommandParameters {

    private final EnumSet<T> enums;
    private final boolean isHelp;
    private final boolean isListing;
    private final boolean isAdding;
    private final boolean isRemoving;
    private final User user;

    public EnumListParameters(Context e, EnumSet<T> enums, boolean isHelp, boolean isListing, boolean isAdding, boolean isRemoving, User user) {
        super(e);
        this.enums = enums;
        this.isHelp = isHelp;
        this.isListing = isListing;
        this.isAdding = isAdding;
        this.isRemoving = isRemoving;
        this.user = user;
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

    public boolean isAdding() {
        return isAdding;
    }

    public boolean isRemoving() {
        return isRemoving;
    }

    public User getUser() {
        return user;
    }
}
