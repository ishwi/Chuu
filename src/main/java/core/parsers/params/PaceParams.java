package core.parsers.params;

import core.commands.Context;

public class PaceParams extends NumberParameters<NumberParameters<NaturalTimeParams>> {

    public PaceParams(Context e, CountableNaturalTimeParams innerParams, Long extraParam) {
        super(e, innerParams, extraParam);
    }
}
