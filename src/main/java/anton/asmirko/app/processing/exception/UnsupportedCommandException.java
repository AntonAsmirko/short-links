package anton.asmirko.app.processing.exception;

public class UnsupportedCommandException extends IllegalArgumentException {
  public UnsupportedCommandException(String message) {
    super(message);
  }
}
