package dao.entities;

import java.sql.Date;

public class Week {
    private final int id;
    private final Date weekStart;

    public Week(int id, Date week_start) {
        this.id = id;
        this.weekStart = week_start;
    }

    public int getId() {
        return id;
    }

    public Date getWeekStart() {
        return weekStart;
    }
}
