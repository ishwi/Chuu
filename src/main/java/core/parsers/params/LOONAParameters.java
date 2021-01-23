package core.parsers.params;

import dao.entities.LOONA;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nullable;

public class LOONAParameters extends ChuuDataParams {


    private final SubCommand subCommand;
    private final Display display;
    private final LOONA targetedLOONA;
    private final LOONA.Type targetedType;
    private final Mode mode;
    private final Subject subject;

    public LOONAParameters(MessageReceivedEvent e, LastFMData lastFMData, SubCommand subCommand, Display display, @Nullable LOONA targetedLOONA, LOONA.@org.jetbrains.annotations.Nullable Type targetedType, Subject subject, Mode mode) {
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
        GROUPED, UNGROUPED
    }

    public enum Display {
        COLLAGE, SUM, COUNT
    }

    public enum Subject {
        ME, SERVER
    }

}
