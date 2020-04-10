package core.apis.last.exceptions;

public class ExceptionEntity {
    private static final String FIELD = "user";
    private String userName;

    public ExceptionEntity(String userName) {
        this.userName = userName;
    }


    public String getField() {
        return FIELD;
    }


    public String getUserName() {
        return userName;
    }


}
