package anton.asmirko.app.processing.algorithms;

import anton.asmirko.app.model.commands.TtlGlobalCommand;
import anton.asmirko.app.model.user.AdminUser;
import anton.asmirko.app.model.user.User;
import anton.asmirko.app.model.user.UserFactory;
import anton.asmirko.app.repository.ttl.TtlGlobalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component(TtlGlobalCommand.STR_REP)
@RequiredArgsConstructor
public class TtlGlobalCommandProcessingAlg implements CLICommandProcessingAlg<TtlGlobalCommand> {

  private final TtlGlobalRepository ttlGlobalRepository;
  private final UserFactory userFactory;

  @Override
  public void process(TtlGlobalCommand cliCommand) {
    final User user = userFactory.createUser("admin");
    if (user instanceof AdminUser) {
      ((AdminUser) user).askAuth();
    }
    Integer ttl = cliCommand.ttl();
    if (ttl == null) {
      ttl = ttlGlobalRepository.getTtl();
      System.out.printf("ttl равен %d", ttl);
      return;
    }
    if (ttl < 0) {
      throw new IllegalArgumentException(String.format("ttl не может быть меньше 0: %s", ttl));
    }
    ttlGlobalRepository.setTtl(ttl);
    System.out.printf("Глобальный ttl успешно установлен в %s%n", ttl);
  }
}
