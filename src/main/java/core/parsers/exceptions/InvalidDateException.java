package core.parsers.exceptions;

public class InvalidDateException extends Exception {
    public InvalidDateException() {
    }

    public String getErrorMessage() {
        return "The second date has to happen before the first date";
    }
}
