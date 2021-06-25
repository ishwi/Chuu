package core.parsers.utils;

public enum Optionals {
    LIST(new OptionalEntity("list", "display it in an embed", "l")),
    PIE(new OptionalEntity("pie", "show it as a pie", "p")),
    IMAGE(new OptionalEntity("image", "show this as a chart", "i")),
    GLOBAL(new OptionalEntity("global", "make it global", "g")),
    SERVER(new OptionalEntity("server", "only include people in this server", "s")),
    NOBOTTED(new OptionalEntity("nobotted", "discard users that have been manually flagged as potentially botted accounts", "nb")),
    BOTTED(new OptionalEntity("botted", "show botted accounts in case you have the config show-botted disabled", "b")),
    NOPLAYS(new OptionalEntity("noplays", "hide the plays", "np")),
    PLAYS(new OptionalEntity("plays", "shows this with plays", "p")),
    START(new OptionalEntity("start", "show the moment the streak started")),
    NOREDIRECT(new OptionalEntity("noredirect", "not use autocorrections", "nr")),
    NOTITLES(new OptionalEntity("notitles", "not display titles", "nt")),
    ASIDE(new OptionalEntity("aside", "show titles on the side", "as")),
    ARTIST(new OptionalEntity("artist", "use artists instead of albums", "a")),
    TITLES(new OptionalEntity("titles", "display titles", "t"));

    public final OptionalEntity opt;

    Optionals(OptionalEntity opt) {
        this.opt = opt;
    }


}
