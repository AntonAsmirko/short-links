package anton.asmirko.app.model.user;

public sealed interface User permits AdminUser, UUIDUser {

  String getUuid();
}
