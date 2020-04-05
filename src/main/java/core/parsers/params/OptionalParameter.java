package core.parsers.params;

import core.parsers.OptionalEntity;

public class OptionalParameter {
    private final OptionalEntity opt;
    private final int expectedPosition;

    public OptionalParameter(OptionalEntity opt, int expectedPosition) {
        this.opt = opt;
        this.expectedPosition = expectedPosition;
    }

    public OptionalParameter(String optional, int expectedPosition) {
        this(new OptionalEntity(optional, null), expectedPosition);
    }

    public OptionalEntity getOpt() {
        return opt;
    }

    public int getExpectedPosition() {
        return expectedPosition;
    }
}
