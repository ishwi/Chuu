package core.otherlisteners;

import core.commands.utils.ButtonUtils;
import core.commands.utils.ChuuEmbedBuilder;
import core.commands.utils.CommandUtil;
import dao.ChuuService;
import dao.entities.DiscordUserDisplay;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.awt.*;

public record FriendRequester(ChuuService db) implements ConstantListener {
    @Override
    public boolean isValid(ButtonInteractionEvent e) {
        String id = e.getComponentId();
        return id.startsWith(ButtonUtils.FRIEND_REQUEST_ACCEPT) || id.startsWith(ButtonUtils.FRIEND_REQUEST_REJECT);
    }

    @Override
    public void handleClick(ButtonInteractionEvent e) {
        String id = e.getComponentId();
        String[] split = id.split(":");
        assert split.length == 2;
        String action = split[0];
        long requesterId = Long.parseLong(split[1]);
        final long author = e.getUser().getIdLong();
        switch (action) {
            case ButtonUtils.FRIEND_REQUEST_ACCEPT -> {
                DiscordUserDisplay ui = CommandUtil.getUserInfoUnescaped(requesterId);
                if (!db.acceptRequest(author, requesterId)) {
                    e.getMessage().editMessage(MessageEditData.fromEmbeds(
                            new ChuuEmbedBuilder(false).setAuthor("There was an issue accepting %s's request!".formatted(ui.username()), null, ui.urlImage()).build()
                    )).queue();
                    return;
                }
                e.getMessage().editMessage(MessageEditData.fromEmbeds(new ChuuEmbedBuilder(false)
                                .setAuthor("Accepted %s's friend request!".formatted(ui.username()), null, ui.urlImage()).build()
                        )).flatMap(message -> message.getJDA().openPrivateChannelById(requesterId))
                        .flatMap(privateChannel -> {
                            final DiscordUserDisplay authorUi = CommandUtil.getUserInfoUnescaped(author);
                            return privateChannel.sendMessageEmbeds(new ChuuEmbedBuilder(false)
                                    .setAuthor("%s accepted your friend request!".formatted(authorUi.username()), null, authorUi.urlImage()).build());
                        }).queue();
            }
            case ButtonUtils.FRIEND_REQUEST_REJECT -> {
                db.rejectRequest(author, requesterId);
                DiscordUserDisplay ui = CommandUtil.getUserInfoUnescaped(requesterId);
                e.getMessage().editMessage(MessageEditData.fromEmbeds(new EmbedBuilder().setColor(Color.RED)
                        .setAuthor("Rejected %s's friend request!".formatted(ui.username()), null, ui.urlImage()).build()
                )).queue();
            }
        }
    }
}
