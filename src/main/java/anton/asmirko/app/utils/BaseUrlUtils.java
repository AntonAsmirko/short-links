package anton.asmirko.app.utils;

public class BaseUrlUtils {

  private static final String DEFAULT_BASE = "clck.ru/";

  private BaseUrlUtils() {}

  public static String getBaseUrl() {
    final String base = System.getProperty("link.base");
    return base != null ? base : DEFAULT_BASE;
  }
}
