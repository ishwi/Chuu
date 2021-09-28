package core.parsers.params;

import core.commands.Context;

public class CountableNaturalTimeParams extends NumberParameters<NaturalTimeParams> {

    public CountableNaturalTimeParams(Context e, NaturalTimeParams timeParams, long count) {
        super(e, timeParams, count);
    }
}
