package dao.entities;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public enum LOONA {


    MAIN("MAIN", Type.GROUP), YYXY("YYXY", Type.SUBUNIT), ONETHIRD("1/3", Type.SUBUNIT), OEC("ODD EYE CIRCLE", Type.SUBUNIT),
    CHUU("CHUU", Type.MEMBER), YVES("YVES", Type.MEMBER),
    GOWON("GOWON", Type.MEMBER), HEEJIN("HEEJIN", Type.MEMBER), CHOERRY("CHOERRY", Type.MEMBER),
    HASEUL("HASEUL", Type.MEMBER), OLIVIA("OLIVIA", Type.MEMBER), HYUNJIN("HYUNJIN", Type.MEMBER),
    JINSOUL("JINSOUL", Type.MEMBER), VIVI("VIVI", Type.MEMBER), KIM_LIP("KIM LIP", Type.MEMBER),
    YEOJIN("YEOJIN", Type.MEMBER), MISC("MISC", Type.MISC);
    private static final Map<String, LOONA> ENUM_MAP;

    static {
        ENUM_MAP = Stream.of(LOONA.values())
                .collect(Collectors.toMap(LOONA::getId, Function.identity()));
    }

    private final String id;
    private final Type type;

    LOONA(String id, Type group) {
        this.id = id;
        this.type = group;
    }

    public static LOONA get(String name) {
        return ENUM_MAP.get(name);
    }

    public static String getRepresentative(LOONA.Type type) {
        return switch (type) {
            case GROUP -> "LOONA";
            case SUBUNIT -> "LOONA SUBUNITS";
            case MEMBER -> "LOONA MEMBERS";
            case MISC -> "MISC";
        };
    }

    public static String getRepresentative(LOONA loona) {
        return switch (loona) {
            case MAIN -> "LOONA";
            case YYXY -> "LOONA YYXY";
            case ONETHIRD -> "LOONA 1/3";
            case OEC -> "LOONA OEC";
            case CHUU -> "CHUU";
            case YVES -> "YVES";
            case GOWON -> "GOWON";
            case HEEJIN -> "HEEJIN";
            case CHOERRY -> "CHOERRY";
            case HASEUL -> "HASEUL";
            case OLIVIA -> "OLIVIA HYE";
            case HYUNJIN -> "HYUNJIN";
            case JINSOUL -> "JINSOUL";
            case VIVI -> "VIVI";
            case KIM_LIP -> "KIM LIP";
            case YEOJIN -> "YEOJIN";
            case MISC -> "MISC";
        };
    }

    public static Predicate<String> getTypeParser(Type type) {
        return switch (type) {
            case GROUP -> LOONA.MAIN.getParser();
            case SUBUNIT -> s -> s.equalsIgnoreCase("subunit") || s.equalsIgnoreCase("subunits");
            case MEMBER -> s -> s.equalsIgnoreCase("member") || s.equalsIgnoreCase("members");
            case MISC -> LOONA.MISC.getParser();
        };
    }

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public Predicate<String> getParser() {
        return switch (this) {
            case MAIN -> Pattern.compile("LOONA|LOOΠΔ|이달의 소녀|LOOPIDELTA", Pattern.CASE_INSENSITIVE).asPredicate();
            case YYXY -> Pattern.compile("YYXY|LOOΠΔ|이달의 소녀|LOOPIDELTA", Pattern.CASE_INSENSITIVE).asPredicate();
            case ONETHIRD -> Pattern.compile("1[\\s]*[/\\\\][\\s]*3").asPredicate();
            case OEC -> Pattern.compile("오드아이써클|O(dd)?[\\s]*?e(ye)?[\\s]*C(ircle)?", Pattern.CASE_INSENSITIVE).asPredicate();
            case CHUU -> (s) -> s.equalsIgnoreCase("chuu") || s.equalsIgnoreCase("츄");
            case YVES -> (s) -> s.equalsIgnoreCase("yves") || s.equalsIgnoreCase("이브");
            case GOWON -> Pattern.compile("go[\\s]*won", Pattern.CASE_INSENSITIVE).asPredicate().or(s -> s.equalsIgnoreCase("고원"));
            case HEEJIN -> (s) -> s.equalsIgnoreCase("heejin") || s.equalsIgnoreCase("희진");
            case CHOERRY -> (s) -> s.equalsIgnoreCase("choerry") || s.equalsIgnoreCase("최리");
            case HASEUL -> (s) -> s.equalsIgnoreCase("haseul") || s.equalsIgnoreCase("하슬");
            case OLIVIA -> Pattern.compile("(olivia hye)|(hye)|(olivia)", Pattern.CASE_INSENSITIVE).asPredicate().or(s -> s.equalsIgnoreCase("올리비아"));
            case HYUNJIN -> (s) -> s.equalsIgnoreCase("HYUNJIN") || s.equalsIgnoreCase("현진");
            case JINSOUL -> (s) -> s.equalsIgnoreCase("jinsoul") || s.equalsIgnoreCase("진솔");
            case VIVI -> (s) -> s.equalsIgnoreCase("vivi") || s.equalsIgnoreCase("비비");
            case KIM_LIP -> Pattern.compile("kim[\\s]*lip", Pattern.CASE_INSENSITIVE).asPredicate().or(s -> s.equalsIgnoreCase("김립"));
            case YEOJIN -> (s) -> s.equalsIgnoreCase("YEOJIN") || s.equalsIgnoreCase("여진");
            case MISC -> (s) -> s.toLowerCase().startsWith("misc");
        };
    }

    public enum Type {
        GROUP, SUBUNIT, MEMBER, MISC
    }
}

