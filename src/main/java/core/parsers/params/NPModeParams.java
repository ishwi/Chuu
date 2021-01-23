package core.parsers.params;

import dao.entities.NPMode;

import java.util.EnumSet;

public record NPModeParams(EnumSet<NPMode> modes, boolean isHelp, boolean isListing) {

}
