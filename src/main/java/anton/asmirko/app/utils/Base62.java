package anton.asmirko.app.utils;

public class Base62 {
  private static final String ALPHABET =
      "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  private static final int BASE = ALPHABET.length();

  private Base62() {}

  public static String encode(int num) {
    if (num == 0) return "0";
    boolean negative = num < 0;
    if (negative) num = -num;

    StringBuilder sb = new StringBuilder();
    while (num > 0) {
      int rem = num % BASE;
      sb.append(ALPHABET.charAt(rem));
      num /= BASE;
    }

    if (negative) sb.append('-');
    return sb.reverse().toString();
  }
}
