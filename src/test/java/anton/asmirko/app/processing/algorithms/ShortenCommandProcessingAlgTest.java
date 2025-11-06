package anton.asmirko.app.processing.algorithms;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import anton.asmirko.app.model.commands.ShortenLinkCommand;
import anton.asmirko.app.model.user.AdminUser;
import anton.asmirko.app.model.user.UUIDUser;
import anton.asmirko.app.model.user.UserFactory;
import anton.asmirko.app.repository.links.LinksRepository;
import anton.asmirko.app.repository.shortenedlinks.ShortenedLinksRepository;
import anton.asmirko.app.repository.users.UsersRepository;
import anton.asmirko.app.service.shorten.ShortenLinkService;
import anton.asmirko.app.service.user.user.UUIDService;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShortenCommandProcessingAlgTest {

  private ShortenLinkService shortenLinkService;
  private LinksRepository linksRepository;
  private UUIDService uuidService;
  private UsersRepository usersRepository;
  private ShortenedLinksRepository shortenedLinksRepository;
  private UserFactory userFactory;
  private ShortenCommandProcessingAlg algorithm;

  @BeforeEach
  void setUp() {
    shortenLinkService = mock(ShortenLinkService.class);
    linksRepository = mock(LinksRepository.class);
    uuidService = mock(UUIDService.class);
    usersRepository = mock(UsersRepository.class);
    shortenedLinksRepository = mock(ShortenedLinksRepository.class);
    userFactory = mock(UserFactory.class);
    algorithm =
        new ShortenCommandProcessingAlg(
            shortenLinkService,
            linksRepository,
            uuidService,
            usersRepository,
            shortenedLinksRepository,
            userFactory);
  }

  private ShortenLinkCommand makeCommand(String uri, Map<ShortenLinkCommand.Key, String> keys) {
    return new ShortenLinkCommand(URI.create(uri), keys);
  }

  @Test
  void process_createsNewUser_whenUuidIsNull() {
    var cmd = makeCommand("https://example.com", Map.of());
    when(uuidService.getUUID()).thenReturn("new-user-uuid");
    when(usersRepository.getUUID("new-user-uuid")).thenReturn(100);
    when(shortenLinkService.shortenLink(eq("new-user-uuid"), any())).thenReturn("shortX");
    when(linksRepository.getId("https://example.com")).thenReturn(1);

    algorithm.process(cmd);

    verify(usersRepository).addUser("new-user-uuid");
    verify(shortenedLinksRepository)
        .saveShortened(
            eq(100),
            eq(1),
            contains("shortX"),
            any(LocalDateTime.class),
            isNull(),
            eq(0),
            isNull());
  }

  @Test
  void process_usesExistingUser_whenUuidProvided() {
    var cmd = makeCommand("https://site.org", Map.of(ShortenLinkCommand.Key.USER, "user123"));
    when(userFactory.createUser("user123")).thenReturn(new UUIDUser("user123"));
    when(usersRepository.getUUID("user123")).thenReturn(55);
    when(linksRepository.getId("https://site.org")).thenReturn(10);
    when(shortenLinkService.shortenLink("user123", URI.create("https://site.org")))
        .thenReturn("abc");
    algorithm.process(cmd);

    verify(shortenedLinksRepository)
        .saveShortened(
            eq(55), eq(10), contains("abc"), any(LocalDateTime.class), isNull(), eq(0), isNull());
  }

  @Test
  void process_callsAskAuth_whenAdminUser() {
    var cmd = makeCommand("https://secure.com", Map.of(ShortenLinkCommand.Key.USER, "admin"));
    AdminUser admin = spy(new AdminUser("root"));
    doNothing().when(admin).askAuth();

    when(userFactory.createUser("admin")).thenReturn(admin);
    when(usersRepository.getUUID("admin")).thenReturn(1);
    when(linksRepository.getId("https://secure.com")).thenReturn(1);
    when(shortenLinkService.shortenLink("admin", URI.create("https://secure.com")))
        .thenReturn("secure");

    algorithm.process(cmd);

    verify(admin).askAuth();
  }

  @Test
  void process_throws_whenUserUnknown() {
    var cmd = makeCommand("https://nope.com", Map.of(ShortenLinkCommand.Key.USER, "ghost"));
    when(userFactory.createUser("ghost")).thenReturn(new UUIDUser("ghost"));
    when(usersRepository.getUUID("ghost")).thenReturn(null);
    assertThrows(IllegalArgumentException.class, () -> algorithm.process(cmd));
  }

  @Test
  void process_appliesTtlAndQueryLimit_whenProvided() {
    var cmd =
        makeCommand(
            "https://ttl.com",
            Map.of(
                ShortenLinkCommand.Key.USER, "userx",
                ShortenLinkCommand.Key.TTL, "3600",
                ShortenLinkCommand.Key.NUM_QUERIES, "5"));

    when(userFactory.createUser("userx")).thenReturn(new UUIDUser("userx"));
    when(usersRepository.getUUID("userx")).thenReturn(200);
    when(linksRepository.getId("https://ttl.com")).thenReturn(300);
    when(shortenLinkService.shortenLink("userx", URI.create("https://ttl.com"))).thenReturn("qwe");

    algorithm.process(cmd);

    verify(shortenedLinksRepository)
        .saveShortened(
            eq(200),
            eq(300),
            contains("qwe"),
            any(LocalDateTime.class), // createdAt
            any(LocalDateTime.class), // expiresAt (with TTL)
            eq(0),
            eq(5));
  }

  @Test
  void process_throws_whenLinkIdIsNull_afterInsertIfAbsent() {
    var cmd = makeCommand("https://bad.com", Map.of(ShortenLinkCommand.Key.USER, "user1"));
    when(userFactory.createUser("user1")).thenReturn(new UUIDUser("user1"));
    when(usersRepository.getUUID("user1")).thenReturn(10);
    when(linksRepository.getId("https://bad.com")).thenReturn(null);

    assertThrows(IllegalStateException.class, () -> algorithm.process(cmd));
  }

  @Test
  void process_insertsLinkIfAbsent_andCallsSaveShortened() {
    var cmd = makeCommand("https://insert.com", Map.of(ShortenLinkCommand.Key.USER, "userY"));
    when(userFactory.createUser("userY")).thenReturn(new UUIDUser("userY"));
    when(usersRepository.getUUID("userY")).thenReturn(99);
    when(linksRepository.getId("https://insert.com")).thenReturn(10);
    when(shortenLinkService.shortenLink("userY", URI.create("https://insert.com")))
        .thenReturn("zzz");

    algorithm.process(cmd);

    verify(linksRepository).insertIfAbsent("https://insert.com");
    verify(shortenedLinksRepository)
        .saveShortened(
            eq(99), eq(10), contains("zzz"), any(LocalDateTime.class), isNull(), eq(0), isNull());
  }
}
