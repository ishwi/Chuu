package dao.utils;

public enum Order {
    ASC, DESC;


    public Order getInverse() {
        return switch (this) {
            case ASC -> DESC;
            case DESC -> ASC;
        };
    }

}
