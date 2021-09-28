package core.parsers.params;

import core.commands.Context;
import core.parsers.utils.OptionalEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CommandParameters {
    final Map<OptionalEntity, Boolean> optionals = new HashMap<>();
    private final Context e;

    public CommandParameters(Context e) {
        this.e = e;
    }


    public void initParams(Collection<String> optionals) {
        optionals.forEach(x -> this.optionals.put(new OptionalEntity(x, ""), true));
    }

    public void addOpts(String opt) {
        this.optionals.put(new OptionalEntity(opt, ""), true);
    }


    public Context getE() {
        return e;
    }

    public boolean hasOptional(String optional) {

        return optionals.getOrDefault(new OptionalEntity(optional, null), false);
    }


}
