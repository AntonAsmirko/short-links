package anton.asmirko.app.processing.algorithms;

import anton.asmirko.app.model.commands.OpenLinkCommand;
import anton.asmirko.app.model.user.AdminUser;
import anton.asmirko.app.model.user.User;
import anton.asmirko.app.model.user.UserFactory;
import anton.asmirko.app.repository.shortenedlinks.LinkDao;
import anton.asmirko.app.repository.shortenedlinks.ShortenedLinksRepository;
import anton.asmirko.app.repository.ttl.TtlGlobalRepository;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component(OpenLinkCommand.STR_REP)
@RequiredArgsConstructor
public class OpenLinkCommandProcessingAlg implements CLICommandProcessingAlg<OpenLinkCommand> {

  private final ShortenedLinksRepository shortenedLinksRepository;
  private final UserFactory userFactory;
  private final TtlGlobalRepository ttlGlobalRepository;

  @Override
  public void process(OpenLinkCommand cliCommand) {
    final var keys = cliCommand.keys();
    final String uuid = keys.get(OpenLinkCommand.Key.USER);
    if (uuid == null) {
      throw new IllegalArgumentException("Для команды open необходим параметр -u");
    }

    final User user = userFactory.createUser(uuid);
    if (user instanceof AdminUser) {
      ((AdminUser) user).askAuth();
    }

    final LinkDao linkDao = shortenedLinksRepository.getLink(cliCommand.uri());

    if (linkDao == null) {
      throw new IllegalStateException(
          String.format("Нет активных сокращений ссылок %s", cliCommand.uri()));
    }

    if (linkDao.expiresAt() != null && LocalDateTime.now().isAfter(linkDao.expiresAt())) {
      shortenedLinksRepository.deleteLinkById(linkDao.id());
      throw new IllegalStateException(
          String.format(
              "Время жизни ссылки %s истекло %s",
              cliCommand.uri(), linkDao.expiresAt().format(DateTimeFormatter.ISO_DATE_TIME)));
    }

    final Integer globalTtl = ttlGlobalRepository.getTtl();
    if (globalTtl != null) {
      final LocalDateTime globalExpirationTime = linkDao.createdAt().plusSeconds(globalTtl);
      if (globalExpirationTime.isBefore(LocalDateTime.now())) {
        throw new IllegalStateException(
            String.format(
                "Время жизни ссылки %s истекло %s",
                cliCommand.uri(), globalExpirationTime.format(DateTimeFormatter.ISO_DATE_TIME)));
      }
    }

    if (linkDao.queriesLimit() != null && linkDao.numOfQueries() >= linkDao.queriesLimit()) {
      shortenedLinksRepository.deleteLinkById(linkDao.id());
      throw new IllegalStateException(
          String.format("Лимит запросов по ссылке %s исчерпан", cliCommand.uri()));
    }

    if (!uuid.equals(linkDao.uuid())) {
      throw new IllegalStateException(
          String.format("У данного пользователя нет такого сокращения %s", cliCommand.uri()));
    }

    shortenedLinksRepository.updateNumQueries(linkDao.id(), linkDao.numOfQueries() + 1);

    try {
      Desktop.getDesktop().browse(new URI(linkDao.original()));
    } catch (URISyntaxException | IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
