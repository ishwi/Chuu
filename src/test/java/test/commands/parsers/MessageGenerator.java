package test.commands.parsers;

import core.commands.Context;
import core.commands.ContextMessageReceived;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.mockito.Mockito;

import java.util.ArrayList;

public class MessageGenerator {

    public Context generateMessage(String content) {
        Message mock = Mockito.mock(Message.class, invocation ->
                switch (invocation.getMethod().getName().toLowerCase()) {
                    case "getMentionedMembers", "getMentionedUsers" -> new ArrayList<>();
                    case "getContentRaw" -> content;
                    default -> null;
                });
        return new ContextMessageReceived(new MessageReceivedEvent(null, 0, mock) {
            @Override
            public boolean isFromGuild() {
                return true;
            }
        });

    }
}
