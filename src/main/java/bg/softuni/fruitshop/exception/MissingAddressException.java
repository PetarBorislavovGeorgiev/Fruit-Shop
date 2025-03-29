package bg.softuni.fruitshop.exception;

public class MissingAddressException extends RuntimeException {
    public MissingAddressException(String message) {
        super(message);
    }
}
