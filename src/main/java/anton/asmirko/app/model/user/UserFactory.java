package anton.asmirko.app.model.user;

import anton.asmirko.app.config.AdminConfig;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserFactory {

  private final AdminConfig adminConfig;

  public User createUser(final String strRep) {
    if (AdminUser.PATTERN.matcher(strRep).matches()) {
      return new AdminUser(adminConfig.password);
    }
    if (UUIDUser.PATTERN.matcher(strRep).matches()) {
      return new UUIDUser(strRep);
    }
    throw new IllegalArgumentException(String.format("Невалидный формат пользователя %s", strRep));
  }
}
