package anton.asmirko.app.processing.algorithms;

import anton.asmirko.app.model.commands.PruneCommand;
import anton.asmirko.app.model.user.AdminUser;
import anton.asmirko.app.model.user.User;
import anton.asmirko.app.model.user.UserFactory;
import anton.asmirko.app.repository.shortenedlinks.ShortenedLinksRepository;
import anton.asmirko.app.repository.users.UsersRepository;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component(PruneCommand.STR_REP)
@RequiredArgsConstructor
public class PruneCommandProcessingAlg implements CLICommandProcessingAlg<PruneCommand> {

  private final UserFactory userFactory;
  private final ShortenedLinksRepository shortenedLinksRepository;
  private final UsersRepository usersRepository;

  @Override
  public void process(PruneCommand cliCommand) {
    final String uri = cliCommand.uri();
    final String uuid = cliCommand.keys().get(PruneCommand.Key.USER);
    if (uri != null && uuid != null) {
      final User user = getUser(uuid);
      final Integer userId = usersRepository.getUUID(user.getUuid());
      if (userId == null) {
        throw new IllegalStateException(
            String.format("Не найден пользователь с uuid: %s", user.getUuid()));
      }
      final int recordsDeleted = shortenedLinksRepository.deleteUsersLink(userId, uri);
      if (recordsDeleted == 0) {
        throw new IllegalStateException(
            String.format("Ссылка %s не пренадлежит пользователю %s", uri, uuid));
      }
      System.out.printf("Ссыка %s пользователя %s удалена%n", uri, uuid);
    } else if (uri != null) {
      final User user = getUser("admin");
      final int recordsDeleted = shortenedLinksRepository.deleteLink(uri);
      if (recordsDeleted == 0) {
        throw new IllegalStateException(String.format("Ссылки %s не существует", uri));
      }
      System.out.printf("Ссыка %s удалена пользователем admin", uri);
    } else if (uuid != null) {
      final User user = getUser(uuid);
      final Integer userId = usersRepository.getUUID(user.getUuid());
      if (userId == null) {
        throw new IllegalStateException(
            String.format("Не найден пользователь с uuid: %s", user.getUuid()));
      }
      final AtomicInteger counter = new AtomicInteger(0);
      final List<String> userLinks = shortenedLinksRepository.getUsersLinks(userId);
      userLinks.forEach(
          it -> {
            shortenedLinksRepository.deleteUsersLink(userId, it);
            counter.incrementAndGet();
            System.out.printf("Ссыка %s пользователя %s удалена%n", it, uuid);
          });
      System.out.printf("Удалено %d ссылок пользователя %s%n", counter.get(), uuid);
    } else {
      throw new IllegalArgumentException(
          "Для команды prune необходимо предоставить uri или пользователя");
    }
  }

  private User getUser(final String uuid) {
    final User user = userFactory.createUser(uuid);
    if (user instanceof AdminUser) {
      ((AdminUser) user).askAuth();
    }
    return user;
  }
}
