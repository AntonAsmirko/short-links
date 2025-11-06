package anton.asmirko.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import anton.asmirko.app.CLI.CLIReader;
import anton.asmirko.app.model.commands.*;
import anton.asmirko.app.model.user.UUIDUser;
import anton.asmirko.app.model.user.UserFactory;
import anton.asmirko.app.processing.CLICommandProcessor;
import anton.asmirko.app.processing.algorithms.*;
import anton.asmirko.app.repository.links.LinksRepository;
import anton.asmirko.app.repository.shortenedlinks.ShortenedLinksRepository;
import anton.asmirko.app.repository.ttl.TtlGlobalRepository;
import anton.asmirko.app.repository.users.UsersRepository;
import anton.asmirko.app.service.shorten.ShortenLinkService;
import anton.asmirko.app.service.user.user.UUIDService;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShortLinksAppDeepIntegrationTest {

  private ShortenLinkService shortenLinkService;
  private LinksRepository linksRepository;
  private ShortenedLinksRepository shortenedLinksRepository;
  private UsersRepository usersRepository;
  private TtlGlobalRepository ttlGlobalRepository;
  private UUIDService uuidService;
  private UserFactory userFactory;

  private ShortenCommandProcessingAlg shortenAlg;
  private TtlGlobalCommandProcessingAlg ttlAlg;

  private CLIReader cliReader;
  private CLICommandProcessor processor;
  private ShortLinksApp app;

  @BeforeEach
  void setUp() {
    shortenLinkService = mock(ShortenLinkService.class);
    linksRepository = mock(LinksRepository.class);
    shortenedLinksRepository = mock(ShortenedLinksRepository.class);
    usersRepository = mock(UsersRepository.class);
    ttlGlobalRepository = mock(TtlGlobalRepository.class);
    uuidService = mock(UUIDService.class);
    userFactory = mock(UserFactory.class);

    shortenAlg =
        new ShortenCommandProcessingAlg(
            shortenLinkService,
            linksRepository,
            uuidService,
            usersRepository,
            shortenedLinksRepository,
            userFactory);

    ttlAlg = new TtlGlobalCommandProcessingAlg(ttlGlobalRepository, userFactory);

    processor =
        new CLICommandProcessor(
            Map.of(
                ShortenLinkCommand.STR_REP, shortenAlg,
                TtlGlobalCommand.STR_REP, ttlAlg));

    cliReader = mock(CLIReader.class);
    app = new ShortLinksApp(cliReader, processor);
  }

  @Test
  void fullFlow_shortenCommand_createsLinkAndUser() {
    URI link = URI.create("https://example.com");
    ShortenLinkCommand cmd = new ShortenLinkCommand(link, Map.of());
    when(cliReader.readArgs(any())).thenReturn(cmd);
    when(uuidService.getUUID()).thenReturn("user-123");
    when(usersRepository.getUUID("user-123")).thenReturn(10);
    when(shortenLinkService.shortenLink(eq("user-123"), eq(link))).thenReturn("abc");
    when(linksRepository.getId("https://example.com")).thenReturn(20);
    when(userFactory.createUser(any())).thenReturn(new UUIDUser("user-123"));

    assertDoesNotThrow(() -> app.run(new String[] {"shorten", "https://example.com"}));

    verify(uuidService).getUUID();
    verify(usersRepository).addUser("user-123");
    verify(linksRepository).insertIfAbsent("https://example.com");
    verify(shortenedLinksRepository)
        .saveShortened(
            eq(10), eq(20), contains("abc"), any(LocalDateTime.class), isNull(), eq(0), isNull());
  }

  @Test
  void fullFlow_shortenCommand_existingUserWithTtl() {
    URI link = URI.create("https://ttl.com");
    ShortenLinkCommand cmd =
        new ShortenLinkCommand(
            link,
            Map.of(
                ShortenLinkCommand.Key.USER, "uuid-55",
                ShortenLinkCommand.Key.TTL, "3600"));
    when(cliReader.readArgs(any())).thenReturn(cmd);
    when(userFactory.createUser("uuid-55")).thenReturn(new UUIDUser("uuid-55"));
    when(usersRepository.getUUID("uuid-55")).thenReturn(55);
    when(linksRepository.getId("https://ttl.com")).thenReturn(5);
    when(shortenLinkService.shortenLink("uuid-55", link)).thenReturn("zzz");

    assertDoesNotThrow(() -> app.run(new String[] {"shorten", "https://ttl.com"}));

    verify(shortenedLinksRepository)
        .saveShortened(
            eq(55),
            eq(5),
            contains("zzz"),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            eq(0),
            isNull());
  }

  @Test
  void fullFlow_ttlGlobal_setsNewValue() {
    TtlGlobalCommand cmd = new TtlGlobalCommand(600);
    when(cliReader.readArgs(any())).thenReturn(cmd);
    when(userFactory.createUser("admin")).thenReturn(new UUIDUser("admin"));

    assertDoesNotThrow(() -> app.run(new String[] {"ttlGlobal", "600"}));

    verify(ttlGlobalRepository).setTtl(600);
  }

  @Test
  void fullFlow_ttlGlobal_readsValue_whenNoArg() {
    TtlGlobalCommand cmd = new TtlGlobalCommand(null);
    when(cliReader.readArgs(any())).thenReturn(cmd);
    when(userFactory.createUser("admin")).thenReturn(new UUIDUser("admin"));
    when(ttlGlobalRepository.getTtl()).thenReturn(1234);

    assertDoesNotThrow(() -> app.run(new String[] {"ttlGlobal"}));

    verify(ttlGlobalRepository).getTtl();
    verify(ttlGlobalRepository, never()).setTtl(anyInt());
  }
}
