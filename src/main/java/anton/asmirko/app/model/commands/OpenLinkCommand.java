package anton.asmirko.app.model.commands;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.lang.NonNull;

public record OpenLinkCommand(@NonNull String uri, @NonNull Map<Key, String> keys)
    implements CLICommand {

  public static final String STR_REP = "open";

  @Override
  public String getStrRep() {
    return STR_REP;
  }

  public enum Key implements KeyValueValidator {
    USER(
        "-u",
        "^(?:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}|admin)$");

    private final String strRep;
    private final Pattern pattern;

    Key(String strRep, String regexPattern) {
      this.strRep = strRep;
      if (regexPattern != null) {
        this.pattern = Pattern.compile(regexPattern);
      } else {
        this.pattern = null;
      }
    }

    @Override
    public boolean isValid(final String value) {
      final Matcher matcher = pattern.matcher(value);
      return matcher.matches();
    }

    @Override
    public int getArgCount() {
      return pattern != null ? 1 : 0;
    }

    @Override
    public String toString() {
      return strRep;
    }
  }
}
