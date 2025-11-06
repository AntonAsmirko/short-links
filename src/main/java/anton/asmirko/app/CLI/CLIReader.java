package anton.asmirko.app.CLI;

import anton.asmirko.app.CLI.exception.IllegalCommandException;
import anton.asmirko.app.model.commands.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CLIReader {

  public CLICommand readArgs(final String[] args) {
    if (args.length == 0) {
      return new HelpCommand();
    }
    final String command = args[0];
    CLICommand resCommand;
    try {
      resCommand =
          switch (command) {
            case ShortenLinkCommand.STR_REP -> readShortenLinkCommand(args);
            case OpenLinkCommand.STR_REP -> readOpenLinkCommand(args);
            case TtlGlobalCommand.STR_REP -> readTtlGlobalCommand(args);
            case PruneCommand.STR_REP -> readPruneCommand(args);
            default -> new HelpCommand();
          };
    } catch (IllegalCommandException e) {
      System.err.println(e.getMessage());
      resCommand = new HelpCommand();
    }
    return resCommand;
  }

  private PruneCommand readPruneCommand(final String[] args) {
    final String absentUriMsg = "Для команды prune необходимо предоставить uri или пользователя";
    if (args.length < 2) {
      throw new IllegalArgumentException(absentUriMsg);
    }
    final var res = readCommandKeys(args, PruneCommand.Key.class);
    final var keys = res.keys;
    final int pos = res.pos;
    if (pos >= args.length) {
      return new PruneCommand(null, keys);
    }
    return new PruneCommand(args[pos], keys);
  }

  private TtlGlobalCommand readTtlGlobalCommand(final String[] args) {
    if (args.length < 2) {
      return new TtlGlobalCommand(null);
    }

    final Integer ttl = Integer.parseInt(args[1]);
    return new TtlGlobalCommand(ttl);
  }

  private OpenLinkCommand readOpenLinkCommand(final String[] args) {
    final String absentUriMsg = "Для команды open необходимо предоставить uri";
    if (args.length < 2) {
      throw new IllegalCommandException(absentUriMsg);
    }
    var res = readCommandKeys(args, OpenLinkCommand.Key.class);
    final String uri = args[res.pos];
    return new OpenLinkCommand(uri, res.keys);
  }

  private ShortenLinkCommand readShortenLinkCommand(final String[] args) {
    final String absentUriMsg = "Для команды shorten необходимо предоставить uri";
    if (args.length < 2) {
      throw new IllegalCommandException(absentUriMsg);
    }
    if (args.length == 2) {
      final URI uri = readUri(args[1]);
      return new ShortenLinkCommand(uri, Map.of());
    }
    var res = readCommandKeys(args, ShortenLinkCommand.Key.class);
    if (res.pos == args.length) {
      throw new IllegalCommandException(absentUriMsg);
    }
    final URI uri = readUri(args[res.pos]);
    return new ShortenLinkCommand(uri, res.keys);
  }

  private <E extends Enum<E> & KeyValueValidator> ReadCommandKeysResult<E> readCommandKeys(
      final String[] args, final Class<E> keyType) {
    final String missingKeyValueMsg = "Отсутствует значение для ключа %s для команды %s";
    final Map<String, List<E>> keysSet =
        EnumSet.allOf(keyType).stream()
            .map(it -> new Case<>(it.toString(), it))
            .collect(
                Collectors.groupingBy(
                    it -> it.key, Collectors.mapping(it -> it.value, Collectors.toList())));
    final EnumMap<E, String> resultKeys = new EnumMap<>(keyType);
    int i = 1;
    while (i < args.length) {
      final String curKey = args[i];
      if (!keysSet.containsKey(curKey)) {
        break;
      }
      final E key = keysSet.get(curKey).get(0);
      final int argCount = key.getArgCount();
      if (argCount == 0) {
        resultKeys.put(key, null);
        i++;
      } else {
        if (i < args.length - 1) {
          final String curValue = args[i + 1];
          final boolean isValidValue = key.isValid(curValue);
          if (isValidValue) {
            resultKeys.put(key, curValue);
          } else {
            throw new IllegalArgumentException(
                String.format("Невалидное значения для параметра %s: %s", curKey, curValue));
          }
          i += 2;
        } else {
          throw new IllegalArgumentException(String.format(missingKeyValueMsg, curKey, args[0]));
        }
      }
    }
    return new ReadCommandKeysResult<>(resultKeys, i);
  }

  private record Case<T>(String key, T value) {}

  private record ReadCommandKeysResult<E>(Map<E, String> keys, int pos) {}

  private URI readUri(final String uri) {
    final String incorrectUriMsg = "В программу передана некорректная ссылка %s";
    final Pattern httpUriPattern =
        Pattern.compile(
            "^(https?)://"
                + "([\\w\\-._~%]+(?:\\.[\\w\\-._~%]+)+)"
                + "(?::\\d{1,5})?"
                + "(/[\\w\\-._~%!$&'()*+,;=:@/?]*)?$",
            Pattern.CASE_INSENSITIVE);
    final Matcher matcher = httpUriPattern.matcher(uri);
    if (!matcher.matches()) {
      throw new IllegalCommandException(String.format(incorrectUriMsg, uri));
    }
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new IllegalCommandException(String.format(incorrectUriMsg, uri));
    }
  }
}
