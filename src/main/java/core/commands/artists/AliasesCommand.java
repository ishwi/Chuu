package core.commands.artists;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.otherlisteners.Reactionary;
import core.parsers.ArtistParser;
import core.parsers.Parser;
import core.parsers.params.ArtistParameters;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

public class AliasesCommand extends ConcurrentCommand<ArtistParameters> {

    private final Spotify spotify;
    private final DiscogsApi discogsApi;

    public AliasesCommand(ChuuService dao) {
        super(dao);
        this.spotify = SpotifySingleton.getInstance();
        this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.INFO;
    }

    @Override
    public Parser<ArtistParameters> initParser() {
        return new ArtistParser(getService(), lastFM);
    }


    @Override
    public String getDescription() {
        return "List all the aliases or corrections of one artist ";
    }

    @Override
    public List<String> getAliases() {
        return List.of("aliases", "corrections");
    }

    @Override
    public String getName() {
        return "Aliases";
    }

    @Override
    protected void onCommand(MessageReceivedEvent e, @NotNull ArtistParameters params) throws LastFmException, InstanceNotFoundException {
        String artist = params.getArtist();
        char prefix = CommandUtil.getMessagePrefix(e);

        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artist, 0, null);
        CommandUtil.validate(getService(), scrobbledArtist, lastFM, discogsApi, spotify);

        String correctedArtist = CommandUtil.cleanMarkdownCharacter(scrobbledArtist.getArtist());
        List<String> artistAliases = getService().getArtistAliases(scrobbledArtist.getArtistId())
                .stream().map(x -> ". **" + CommandUtil.cleanMarkdownCharacter(x) + "**\n").collect(Collectors.toList());
        if (artistAliases.isEmpty()) {
            sendMessageQueue(e, correctedArtist + " doesn't have any correction:");
            return;
        }

        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 10 && i < artistAliases.size(); i++) {
            a.append(i + 1).append(artistAliases.get(i));
        }


        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setDescription(a)
                .setColor(CommandUtil.randomColor())
                .setTitle(correctedArtist + "'s aliases")
                .setFooter("You can submit an alias using " + prefix + "alias", null)
                .setThumbnail(scrobbledArtist.getUrl());
        e.getChannel().sendMessage(new MessageBuilder().setEmbed(embedBuilder.build()).build()).queue(message1 ->
                new Reactionary<>(artistAliases, message1, embedBuilder));
    }

}
