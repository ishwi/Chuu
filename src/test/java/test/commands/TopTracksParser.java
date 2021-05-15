package test.commands;

import org.junit.Test;
import test.commands.parsers.NullReturnParsersTest;
import test.commands.utils.CommandTest;
import test.commands.utils.EmbedUtils;

public class TopTracksParser extends CommandTest {
    @Override
    public String giveCommandName() {
        return "!tt";
    }

    @Test
    @Override
    public void nullParserReturned() {
        NullReturnParsersTest.timerFrameParser(COMMAND_ALIAS);
    }

    @Test
    public void normalExample() {

        EmbedUtils
                .testLeaderboardEmbed(COMMAND_ALIAS, EmbedUtils.descriptionArtistAlbumRegex, "${header}'s top  tracks in .*?", false, false, null
                );
    }
}
