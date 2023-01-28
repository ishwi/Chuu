package core.parsers.utils;

import core.commands.utils.CommandUtil;
import dao.entities.NaturalTimeFrameEnum;
import dao.entities.TimeFrameEnum;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Objects;

public class CustomTimeFrame {

    private final Type type;
    private final NaturalTimeFrameEnum naturalTimeFrameEnum;
    private final TimeFrameEnum timeFrameEnum;
    private final OffsetDateTime from;
    private final OffsetDateTime to;
    private long count;

    public CustomTimeFrame(OffsetDateTime from, OffsetDateTime to) {
        this.type = Type.CUSTOM;
        this.naturalTimeFrameEnum = null;
        this.from = from;
        this.to = to;
        this.timeFrameEnum = null;

    }


    public CustomTimeFrame(NaturalTimeFrameEnum naturalTimeFrameEnum, long count) {
        this.naturalTimeFrameEnum = naturalTimeFrameEnum;
        this.count = count;
        this.from = naturalTimeFrameEnum.toLocalDate(1).atOffset(ZoneOffset.UTC);
        this.to = null;
        this.type = Type.NATURAL;
        this.timeFrameEnum = null;

    }

    public CustomTimeFrame(@NotNull TimeFrameEnum timeFrameEnum) {
        this.timeFrameEnum = timeFrameEnum;
        this.count = -1;
        this.from = timeFrameEnum.toLocalDate(1).atOffset(ZoneOffset.UTC);
        this.to = null;
        this.type = Type.NORMAL;
        this.naturalTimeFrameEnum = null;
    }

    public static CustomTimeFrame ofTimeFrameEnum(TimeFrameEnum timeFrameEnum) {
        return new CustomTimeFrame(timeFrameEnum);
    }

    public NaturalTimeFrameEnum getNaturalTimeFrameEnum() {
        return naturalTimeFrameEnum;
    }

    public OffsetDateTime getFrom() {
        return from;
    }

    public OffsetDateTime getTo() {
        return to;
    }

    public long getCount() {
        return count;
    }

    public Type getType() {
        return type;
    }

    public TimeFrameEnum getTimeFrameEnum() {
        if (type == Type.NORMAL) {
            return timeFrameEnum;
        } else if (type == Type.NATURAL && isNormal()) {
            return TimeFrameEnum.fromCompletePeriod(getNaturalTimeFrameEnum().toApiFormat());
        }
        return null;
    }

    public boolean isNormal() {
        return switch (type) {
            case NORMAL -> true;
            case NATURAL -> (count == 1 && !EnumSet.of(NaturalTimeFrameEnum.MINUTE, NaturalTimeFrameEnum.SECOND, NaturalTimeFrameEnum.HOUR).contains(naturalTimeFrameEnum));
            case CUSTOM -> false;
        };
    }

    public boolean isAllTime() {
        return switch (type) {
            case NORMAL -> timeFrameEnum == TimeFrameEnum.ALL;
            case NATURAL -> naturalTimeFrameEnum == NaturalTimeFrameEnum.ALL;
            case CUSTOM -> false;
        };

    }

    public String toValueString() {
        return switch (type) {
            case NORMAL -> Objects.requireNonNull(timeFrameEnum).toValueString();
            case NATURAL -> Objects.requireNonNull(naturalTimeFrameEnum).toValueString();
            case CUSTOM -> getDisplayString();
        };
    }

    public String getDisplayString() {
        if (type == Type.NORMAL) {
            assert timeFrameEnum != null;
            return timeFrameEnum.getDisplayString();
        }
        if (type == Type.NATURAL) {
            assert naturalTimeFrameEnum != null;
            return naturalTimeFrameEnum.getDisplayString(getCount());
        }
        assert from != null;
        assert to != null;
        int fromDayOfMonth = from.getDayOfMonth();
        int toDayOfMonth = to.getDayOfMonth();
        String fromDayNumberSuffix = CommandUtil.getDayNumberSuffix(fromDayOfMonth);
        String toDayNumberSuffix = CommandUtil.getDayNumberSuffix(toDayOfMonth);
        long fromEpochSecond = from.toInstant().getEpochSecond();
        long toEpochSecond = to.toInstant().getEpochSecond();
        String fromHourFormat = "";
        String toHourFormat = "";

        // less than 7 days
        if (toEpochSecond - fromEpochSecond < 604800) {
            toHourFormat = " HH:mm";
            if (from.getHour() != 0 || from.getMinute() != 0 || from.getSecond() != 0) {
                fromHourFormat = toHourFormat;
            }
        }
        String fromFormat = DateTimeFormatter.ofPattern("MMMM ").format(from) + DateTimeFormatter.ofPattern("d").format(from) + fromDayNumberSuffix
                + DateTimeFormatter.ofPattern(" yyyy" + fromHourFormat).format(from);
        String toFormat = DateTimeFormatter.ofPattern("MMMM ").format(to) + DateTimeFormatter.ofPattern("d").format(to) + toDayNumberSuffix
                + DateTimeFormatter.ofPattern(" yyyy" + toHourFormat).format(to);

        return " from " + fromFormat + " to " + toFormat;

    }


    public enum Type {
        NORMAL, NATURAL, CUSTOM
    }
}
