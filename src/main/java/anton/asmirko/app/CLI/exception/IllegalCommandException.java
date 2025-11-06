package anton.asmirko.app.CLI.exception;

public class IllegalCommandException extends IllegalArgumentException {
  public IllegalCommandException(String message) {
    super(message);
  }
}
