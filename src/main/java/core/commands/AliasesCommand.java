package core.commands;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
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
    protected CommandCategory getCategory() {
        return CommandCategory.INFO;
    }

    @Override
    public Parser<ArtistParameters> getParser() {
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
    void onCommand(MessageReceivedEvent e) throws LastFmException, InstanceNotFoundException {
        ArtistParameters parse = this.parser.parse(e);
        if (parse == null) {
            return;
        }
        String artist = parse.getArtist();
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


        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(a);
        embedBuilder.setColor(CommandUtil.randomColor());
        embedBuilder.setTitle(correctedArtist + "'s aliases");
        embedBuilder.setFooter("You can submit an alias using " + prefix + "alias", null);
        embedBuilder.setThumbnail(scrobbledArtist.getUrl());
        MessageBuilder mes = new MessageBuilder();
        e.getChannel().sendMessage(mes.setEmbed(embedBuilder.build()).build()).queue(message1 ->
                new Reactionary<>(artistAliases, message1, embedBuilder));
    }

}
