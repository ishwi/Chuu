package core.parsers.params;

import core.commands.Context;
import dao.entities.LOONA;
import dao.entities.LastFMData;
import org.jetbrains.annotations.Nullable;

public class LOONAParameters extends ChuuDataParams {


    private final SubCommand subCommand;
    private final Display display;
    private final LOONA targetedLOONA;
    private final LOONA.Type targetedType;
    private final Mode mode;
    private final Subject subject;

    public LOONAParameters(Context e, LastFMData lastFMData, SubCommand subCommand, Display display, @Nullable LOONA targetedLOONA, LOONA.@org.jetbrains.annotations.Nullable Type targetedType, Subject subject, Mode mode) {
        super(e, lastFMData);
        this.subCommand = subCommand;
        this.display = display;
        this.targetedLOONA = targetedLOONA;
        this.targetedType = targetedType;
        this.subject = subject;
        this.mode = mode;

    }

    public Subject getSubject() {
        return subject;
    }

    public Display getDisplay() {
        return display;
    }

    public LOONA getTargetedLOONA() {
        return targetedLOONA;
    }

    public LOONA.Type getTargetedType() {
        return targetedType;
    }

    public SubCommand getSubCommand() {
        return subCommand;
    }

    public Mode getMode() {
        return mode;
    }

    public enum SubCommand {
        GENERAL, SPECIFIC, GROUPED
    }

    public enum Mode {
        GROUPED, ALL, UNGROUPED
    }

    public enum Display {
        COLLAGE, SUM, COUNT
    }

    public enum Subject {
        ME, SERVER
    }

}
