package anton.asmirko.app.model.commands;

public interface KeyValueValidator {
  boolean isValid(String value);

  int getArgCount();
}
