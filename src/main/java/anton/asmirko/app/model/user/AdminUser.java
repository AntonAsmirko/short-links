package anton.asmirko.app.model.user;

import java.util.Scanner;
import java.util.regex.Pattern;

public record AdminUser(String password) implements User {
  public static final Pattern PATTERN = Pattern.compile("^admin$");

  public void askAuth() {
    try (final Scanner scanner = new Scanner(System.in)) {
      System.out.print("admin password:");
      final String pwd = scanner.nextLine();
      if (!password.equals(pwd)) {
        throw new IllegalStateException("Неверный пароль для администратора");
      }
    }
  }

  @Override
  public String getUuid() {
    return "admin";
  }
}
