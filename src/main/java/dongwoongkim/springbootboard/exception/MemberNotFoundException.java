package dongwoongkim.springbootboard.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException() {
    }

    public MemberNotFoundException(String message) {
        super(message);
    }
}
