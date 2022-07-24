package core.commands.config;

import core.Chuu;
import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.parsers.Parser;
import core.parsers.PrefixParser;
import core.parsers.params.CharacterParameters;
import core.services.PrefixService;
import core.util.ServiceView;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static core.translations.Messages.PREFIX_COMMAND_SUCCESS;
import static core.translations.TranslationManager.m;

public class PrefixCommand extends ConcurrentCommand<CharacterParameters> {
    public PrefixCommand(ServiceView dao) {
        super(dao);
        respondInPrivate = false;
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.CONFIGURATION;
    }

    @Override
    public Parser<CharacterParameters> initParser() {
        return new PrefixParser();
    }

    @Override
    public String getDescription() {
        return "Sets the prefix that the bot will respond to";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("prefix");
    }

    @Override
    public void onCommand(Context e, @Nonnull CharacterParameters params) {

        char newPrefix = params.getaChar();
        db.createGuild(e.getGuild().getIdLong());
        db.addGuildPrefix(e.getGuild().getIdLong(), newPrefix);
        PrefixService prefixService = Chuu.prefixService;
        prefixService.addGuildPrefix(e.getGuild().getIdLong(), newPrefix);
        sendMessageQueue(e, m(PREFIX_COMMAND_SUCCESS, ';'));
    }

    @Override
    public String getName() {
        return "Prefix setter";
    }

}
