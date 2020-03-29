package dao.entities;

public class CountWrapper<T> {
    private final T result;
    private int rows;

    public CountWrapper(int rows, T result) {
        this.rows = rows;
        this.result = result;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public T getResult() {
        return result;
    }
}
