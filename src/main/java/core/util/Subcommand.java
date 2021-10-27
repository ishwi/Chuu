package core.util;

import core.commands.Context;
import core.exceptions.LastFmException;
import core.parsers.Parser;
import core.parsers.SubParser;
import core.parsers.params.CommandParameters;
import dao.exceptions.InstanceNotFoundException;

public interface Subcommand {

    SubcommandEx<?> getSubcommandEx();

    @SuppressWarnings("unchecked")
    default <T extends CommandParameters> T parse(Context e, Deps deps, String args) throws LastFmException, InstanceNotFoundException {
        Parser<?> parser = getSubcommandEx().getParser(deps);
        String[] argsArr = args == null ? new String[]{} : args.split("\\s+");
        SubParser<T> tSubParser = new SubParser<>((Parser<T>) parser, argsArr);
        parser.getOptionals().forEach(tSubParser::addOptional);
        return tSubParser.parse(e);
    }


}
