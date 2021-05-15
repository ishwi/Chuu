package dao.entities;

public class GenreInfo {
    private final String name;
    private final int total;
    private final int reach;
    private final String string;

    public GenreInfo(String name, int total, int reach, String string) {


        this.name = name;
        this.total = total;
        this.reach = reach;
        this.string = string;
    }

    public String getName() {
        return name;
    }

    public int getTotal() {
        return total;
    }

    public int getReach() {
        return reach;
    }

    public String getString() {
        return string;
    }
}
