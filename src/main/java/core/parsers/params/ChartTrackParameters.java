package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartTrackParameters extends ChartParameters {
    public ChartTrackParameters(String[] returned, MessageReceivedEvent e) {
        super(returned, e);
    }


    @Override
    public boolean isList() {
        return !super.isList();
    }

    public boolean isImage() {
        return this.isList();
    }
}
