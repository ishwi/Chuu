package core.commands.friends;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.exceptions.LastFmException;
import core.parsers.EnumParser;
import core.parsers.Parser;
import core.parsers.params.EnumParameters;
import core.util.Deps;
import core.util.ServiceView;
import core.util.Subcommand;
import dao.exceptions.InstanceNotFoundException;
import org.jetbrains.annotations.NotNull;

public abstract class ParentCommmand<T extends Enum<T> & Subcommand> extends ConcurrentCommand<EnumParameters<T>> {

    protected final Deps deps = new Deps(lastFM, db);


    public ParentCommmand(ServiceView dao) {
        super(dao);
    }

    public abstract Class<T> getClazz();

    @Override
    public Parser<EnumParameters<T>> initParser() {
        return new EnumParser<>(getClazz(), true, true, true);
    }

    @Override
    public void onCommand(Context e, @NotNull EnumParameters<T> params) throws LastFmException, InstanceNotFoundException {
        T element = params.getElement();
        if (element == null) {
            showHelp(e, params);
        } else {
            doSubcommand(e, element, params.getParams(), params);
        }
    }

    protected abstract void showHelp(Context e, EnumParameters<T> params);

    public abstract void doSubcommand(Context e, T action, String args, EnumParameters<T> params) throws LastFmException, InstanceNotFoundException;
}
