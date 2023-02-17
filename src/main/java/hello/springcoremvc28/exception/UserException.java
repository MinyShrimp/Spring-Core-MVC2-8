package hello.springcoremvc28.exception;

public class UserException extends RuntimeException {
    public UserException(
            String message
    ) {
        super(message);
    }
}
