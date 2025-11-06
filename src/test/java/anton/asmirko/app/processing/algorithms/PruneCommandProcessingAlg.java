package anton.asmirko.app.processing.algorithms;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import anton.asmirko.app.model.commands.PruneCommand;
import anton.asmirko.app.model.user.AdminUser;
import anton.asmirko.app.model.user.UUIDUser;
import anton.asmirko.app.model.user.UserFactory;
import anton.asmirko.app.repository.shortenedlinks.ShortenedLinksRepository;
import anton.asmirko.app.repository.users.UsersRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PruneCommandProcessingAlgTest {

  private UserFactory userFactory;
  private ShortenedLinksRepository shortenedLinksRepository;
  private UsersRepository usersRepository;
  private PruneCommandProcessingAlg algorithm;

  @BeforeEach
  void setUp() {
    userFactory = mock(UserFactory.class);
    shortenedLinksRepository = mock(ShortenedLinksRepository.class);
    usersRepository = mock(UsersRepository.class);
    algorithm =
        new PruneCommandProcessingAlg(userFactory, shortenedLinksRepository, usersRepository);
  }

  private PruneCommand makeCommand(String uri, String uuid) {
    Map<PruneCommand.Key, String> keys =
        uuid == null ? Map.of() : Map.of(PruneCommand.Key.USER, uuid);
    return new PruneCommand(uri, keys);
  }

  @Test
  void process_deletesSpecificUserLink_whenUriAndUuidProvided() {
    var cmd = makeCommand("https://example.com", "user-uuid");
    when(userFactory.createUser("user-uuid")).thenReturn(new UUIDUser("user-uuid"));
    when(usersRepository.getUUID("user-uuid")).thenReturn(42);
    when(shortenedLinksRepository.deleteUsersLink(42, "https://example.com")).thenReturn(1);

    algorithm.process(cmd);

    verify(shortenedLinksRepository).deleteUsersLink(42, "https://example.com");
  }

  @Test
  void process_throws_whenUserNotFoundByUuid() {
    var cmd = makeCommand("https://example.com", "no-user");
    when(userFactory.createUser("no-user")).thenReturn(new UUIDUser("no-user"));
    when(usersRepository.getUUID("no-user")).thenReturn(null);

    IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> algorithm.process(cmd));
    assertTrue(ex.getMessage().contains("Не найден пользователь"));
  }

  @Test
  void process_throws_whenUserHasNoSuchLink() {
    var cmd = makeCommand("https://nope.com", "userX");
    when(userFactory.createUser("userX")).thenReturn(new UUIDUser("userX"));
    when(usersRepository.getUUID("userX")).thenReturn(77);
    when(shortenedLinksRepository.deleteUsersLink(77, "https://nope.com")).thenReturn(0);

    IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> algorithm.process(cmd));
    assertTrue(ex.getMessage().contains("Ссылка"));
  }

  @Test
  void process_deletesLinkAsAdmin_whenOnlyUriProvided() {
    var cmd = makeCommand("https://site.org", null);
    AdminUser admin = spy(new AdminUser("root"));
    doNothing().when(admin).askAuth();

    when(userFactory.createUser("admin")).thenReturn(admin);
    when(shortenedLinksRepository.deleteLink("https://site.org")).thenReturn(1);

    algorithm.process(cmd);

    verify(shortenedLinksRepository).deleteLink("https://site.org");
    verify(admin).askAuth();
  }

  @Test
  void process_throws_whenDeletingAsAdminButLinkNotFound() {
    var cmd = makeCommand("https://missing.org", null);
    AdminUser admin = spy(new AdminUser("root"));
    doNothing().when(admin).askAuth();
    when(userFactory.createUser("admin")).thenReturn(admin);
    when(shortenedLinksRepository.deleteLink("https://missing.org")).thenReturn(0);

    IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> algorithm.process(cmd));
    assertTrue(ex.getMessage().contains("не существует"));
  }

  @Test
  void process_deletesAllUserLinks_whenOnlyUuidProvided() {
    var cmd = makeCommand(null, "userX");
    when(userFactory.createUser("userX")).thenReturn(new UUIDUser("userX"));
    when(usersRepository.getUUID("userX")).thenReturn(99);
    when(shortenedLinksRepository.getUsersLinks(99)).thenReturn(List.of("link1", "link2", "link3"));

    algorithm.process(cmd);

    verify(shortenedLinksRepository, times(3)).deleteUsersLink(eq(99), anyString());
  }

  @Test
  void process_throws_whenOnlyUuidProvided_butUserNotFound() {
    var cmd = makeCommand(null, "ghost");
    when(userFactory.createUser("ghost")).thenReturn(new UUIDUser("ghost"));
    when(usersRepository.getUUID("ghost")).thenReturn(null);

    IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> algorithm.process(cmd));
    assertTrue(ex.getMessage().contains("Не найден пользователь"));
  }

  @Test
  void process_throws_whenNeitherUriNorUuidProvided() {
    var cmd = makeCommand(null, null);
    assertThrows(IllegalArgumentException.class, () -> algorithm.process(cmd));
  }
}
