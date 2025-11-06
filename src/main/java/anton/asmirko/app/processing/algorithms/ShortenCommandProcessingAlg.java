package anton.asmirko.app.processing.algorithms;

import anton.asmirko.app.model.commands.ShortenLinkCommand;
import anton.asmirko.app.model.user.AdminUser;
import anton.asmirko.app.model.user.User;
import anton.asmirko.app.model.user.UserFactory;
import anton.asmirko.app.repository.links.LinksRepository;
import anton.asmirko.app.repository.shortenedlinks.ShortenedLinksRepository;
import anton.asmirko.app.repository.users.UsersRepository;
import anton.asmirko.app.service.shorten.ShortenLinkService;
import anton.asmirko.app.service.user.user.UUIDService;
import anton.asmirko.app.utils.BaseUrlUtils;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component(ShortenLinkCommand.STR_REP)
@RequiredArgsConstructor
public class ShortenCommandProcessingAlg implements CLICommandProcessingAlg<ShortenLinkCommand> {

  private final ShortenLinkService shortenLinkService;
  private final LinksRepository linksRepository;
  private final UUIDService uuidService;
  private final UsersRepository usersRepository;
  private final ShortenedLinksRepository shortenedLinksRepository;
  private final UserFactory userFactory;

  @Override
  public void process(final ShortenLinkCommand cliCommand) {
    String uuid = cliCommand.keys().get(ShortenLinkCommand.Key.USER);
    boolean newUser = false;

    Integer userId;

    if (uuid == null) {
      uuid = uuidService.getUUID();
      usersRepository.addUser(uuid);
      userId = usersRepository.getUUID(uuid);
      newUser = true;
    } else {
      final User user = userFactory.createUser(uuid);
      if (user instanceof AdminUser) {
        ((AdminUser) user).askAuth();
      }
      userId = usersRepository.getUUID(uuid);
      if (userId == null) {
        throw new IllegalArgumentException(String.format("Неизвестный пользователь %s", uuid));
      }
    }
    final var keys = cliCommand.keys();

    final URI initial = cliCommand.uri();
    final String initialStr = initial.toString();
    linksRepository.insertIfAbsent(initialStr);
    final Integer initialId =
        Optional.ofNullable(linksRepository.getId(initialStr))
            .orElseThrow(() -> new IllegalStateException("Неконсистентное состояние БД"));

    final String shortened = shortenLinkService.shortenLink(uuid, initial);
    final String shortenedWithBaseUrl = String.format("%s%s", BaseUrlUtils.getBaseUrl(), shortened);

    final LocalDateTime createdAt = LocalDateTime.now();
    final String ttl = keys.get(ShortenLinkCommand.Key.TTL);
    final String numberOfQueriesRaw = keys.get(ShortenLinkCommand.Key.NUM_QUERIES);
    Integer numOfQueries;
    if (numberOfQueriesRaw != null) {
      numOfQueries = Integer.parseInt(numberOfQueriesRaw);
    } else {
      numOfQueries = null;
    }
    final LocalDateTime expiresAt = ttl != null ? createdAt.plusSeconds(Long.parseLong(ttl)) : null;

    shortenedLinksRepository.saveShortened(
        userId, initialId, shortenedWithBaseUrl, createdAt, expiresAt, 0, numOfQueries);

    if (newUser) {
      System.out.printf("Создан новый пользователь %s%n", uuid);
    }
    System.out.println(shortenedWithBaseUrl);
  }
}
