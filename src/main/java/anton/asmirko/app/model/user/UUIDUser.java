package anton.asmirko.app.model.user;

import java.util.regex.Pattern;

public record UUIDUser(String uuid) implements User {
  public static final Pattern PATTERN =
      Pattern.compile(
          "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

  @Override
  public String getUuid() {
    return uuid;
  }
}
