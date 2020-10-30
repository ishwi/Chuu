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
        switch (type) {
            case GROUP:
                return "LOONA";
            case SUBUNIT:
                return "LOONA SUBUNITS";
            case MEMBER:
                return "LOONA MEMBERS";
            case MISC:
                return "MISC";
            default:
                throw new IllegalStateException();
        }
    }

    public static String getRepresentative(LOONA loona) {
        switch (loona) {
            case MAIN:
                return "LOONA";
            case YYXY:
                return "LOONA YYXY";
            case ONETHIRD:
                return "LOONA 1/3";
            case OEC:
                return "LOONA OEC";
            case CHUU:
                return "CHUU";
            case YVES:
                return "YVES";
            case GOWON:
                return "GOWON";
            case HEEJIN:
                return "HEEJIN";
            case CHOERRY:
                return "CHOERRY";

            case HASEUL:
                return "HASEUL";
            case OLIVIA:
                return "OLIVIA HYE";
            case HYUNJIN:
                return "HYUNJIN";
            case JINSOUL:
                return "JINSOUL";
            case VIVI:
                return "VIVI";
            case KIM_LIP:
                return "KIM LIP";
            case YEOJIN:
                return "YEOJIN";
            case MISC:
                return "MISC";
            default:
                throw new IllegalStateException();
        }
    }

    public static Predicate<String> getTypeParser(Type type) {
        switch (type) {
            case GROUP:
                return LOONA.MAIN.getParser();
            case SUBUNIT:
                return s -> s.equalsIgnoreCase("subunit") || s.equalsIgnoreCase("subunits");
            case MEMBER:
                return s -> s.equalsIgnoreCase("member") || s.equalsIgnoreCase("members");
            case MISC:
                return LOONA.MISC.getParser();
            default:
                throw new IllegalStateException();
        }
    }

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public Predicate<String> getParser() {
        switch (this) {
            case MAIN:
                return Pattern.compile("LOONA|LOOΠΔ|이달의 소녀|LOOPIDELTA", Pattern.CASE_INSENSITIVE).asPredicate();
            case YYXY:
                return Pattern.compile("YYXY|LOOΠΔ|이달의 소녀|LOOPIDELTA", Pattern.CASE_INSENSITIVE).asPredicate();
            case ONETHIRD:
                return Pattern.compile("1[\\s]*[/\\\\][\\s]*3").asPredicate();
            case OEC:
                return Pattern.compile("오드아이써클|O(dd)?[\\s]*?e(ye)?[\\s]*C(ircle)?", Pattern.CASE_INSENSITIVE).asPredicate();
            case CHUU:
                return (s) -> s.equalsIgnoreCase("chuu") || s.equalsIgnoreCase("츄");
            case YVES:
                return (s) -> s.equalsIgnoreCase("yves") || s.equalsIgnoreCase("이브");
            case GOWON:
                return Pattern.compile("go[\\s]*won", Pattern.CASE_INSENSITIVE).asPredicate().or(s -> s.equalsIgnoreCase("고원"));
            case HEEJIN:
                return (s) -> s.equalsIgnoreCase("heejin") || s.equalsIgnoreCase("희진");
            case CHOERRY:
                return (s) -> s.equalsIgnoreCase("choerry") || s.equalsIgnoreCase("최리");
            case HASEUL:
                return (s) -> s.equalsIgnoreCase("haseul") || s.equalsIgnoreCase("하슬");
            case OLIVIA:
                return Pattern.compile("(olivia hye)|(hye)|(olivia)", Pattern.CASE_INSENSITIVE).asPredicate().or(s -> s.equalsIgnoreCase("올리비아"));
            case HYUNJIN:
                return (s) -> s.equalsIgnoreCase("HYUNJIN") || s.equalsIgnoreCase("현진");

            case JINSOUL:
                return (s) -> s.equalsIgnoreCase("jinsoul") || s.equalsIgnoreCase("진솔");
            case VIVI:
                return (s) -> s.equalsIgnoreCase("vivi") || s.equalsIgnoreCase("비비");
            case KIM_LIP:
                return Pattern.compile("kim[\\s]*lip", Pattern.CASE_INSENSITIVE).asPredicate().or(s -> s.equalsIgnoreCase("김립"));
            case YEOJIN:
                return (s) -> s.equalsIgnoreCase("YEOJIN") || s.equalsIgnoreCase("여진");
            case MISC:
                return (s) -> s.toLowerCase().startsWith("misc");
            default:
                throw new IllegalStateException();
        }
    }

    public enum Type {
        GROUP, SUBUNIT, MEMBER, MISC
    }
}

