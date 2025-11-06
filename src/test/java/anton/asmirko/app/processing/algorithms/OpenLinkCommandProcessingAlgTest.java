package anton.asmirko.app.processing.algorithms;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import anton.asmirko.app.model.commands.OpenLinkCommand;
import anton.asmirko.app.model.user.UUIDUser;
import anton.asmirko.app.model.user.UserFactory;
import anton.asmirko.app.repository.shortenedlinks.LinkDao;
import anton.asmirko.app.repository.shortenedlinks.ShortenedLinksRepository;
import anton.asmirko.app.repository.ttl.TtlGlobalRepository;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpenLinkCommandProcessingAlgTest {

  private ShortenedLinksRepository shortenedLinksRepository;
  private UserFactory userFactory;
  private TtlGlobalRepository ttlGlobalRepository;
  private OpenLinkCommandProcessingAlg algorithm;

  @BeforeEach
  void setUp() {
    shortenedLinksRepository = mock(ShortenedLinksRepository.class);
    userFactory = mock(UserFactory.class);
    ttlGlobalRepository = mock(TtlGlobalRepository.class);
    algorithm =
        new OpenLinkCommandProcessingAlg(
            shortenedLinksRepository, userFactory, ttlGlobalRepository);
  }

  private OpenLinkCommand makeCommand(String uuid, String uri) {
    return new OpenLinkCommand(uri, Map.of(OpenLinkCommand.Key.USER, uuid));
  }

  private LinkDao makeLinkDao(
      Integer id,
      String uuid,
      String original,
      int numOfQueries,
      Integer queriesLimit,
      LocalDateTime expiresAt,
      LocalDateTime createdAt) {
    return new LinkDao(id, uuid, original, numOfQueries, queriesLimit, expiresAt, createdAt);
  }

  @Test
  void process_throws_whenUserKeyIsMissing() {
    OpenLinkCommand command = new OpenLinkCommand("https://short.ly/x", Map.of());
    assertThrows(IllegalArgumentException.class, () -> algorithm.process(command));
  }

  @Test
  void process_throws_whenLinkNotFound() {
    OpenLinkCommand command = makeCommand("user1", "https://notfound.com");
    when(userFactory.createUser("user1")).thenReturn(new UUIDUser("user1"));
    when(shortenedLinksRepository.getLink("https://notfound.com")).thenReturn(null);
    assertThrows(IllegalStateException.class, () -> algorithm.process(command));
  }

  @Test
  void process_throws_whenLinkExpired() {
    OpenLinkCommand command = makeCommand("user1", "https://short.ly/x");
    LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(5);
    LinkDao expiredLink =
        makeLinkDao(
            5,
            "user1",
            "https://example.com",
            0,
            null,
            expiredTime,
            LocalDateTime.now().minusHours(1));

    when(userFactory.createUser("user1")).thenReturn(new UUIDUser("user1"));
    when(shortenedLinksRepository.getLink("https://short.ly/x")).thenReturn(expiredLink);

    assertThrows(IllegalStateException.class, () -> algorithm.process(command));
    verify(shortenedLinksRepository).deleteLinkById(5);
  }

  @Test
  void process_throws_whenGlobalTtlExpired() {
    OpenLinkCommand command = makeCommand("user1", "https://short.ly/x");
    LinkDao linkDao =
        makeLinkDao(
            3, "user1", "https://example.com", 0, null, null, LocalDateTime.now().minusHours(2));

    when(userFactory.createUser("user1")).thenReturn(new UUIDUser("user1"));
    when(shortenedLinksRepository.getLink("https://short.ly/x")).thenReturn(linkDao);
    when(ttlGlobalRepository.getTtl()).thenReturn(60);

    assertThrows(IllegalStateException.class, () -> algorithm.process(command));
  }

  @Test
  void process_throws_whenQueryLimitReached() {
    OpenLinkCommand command = makeCommand("user1", "https://short.ly/x");
    LinkDao limited =
        makeLinkDao(10, "user1", "https://example.com", 5, 5, null, LocalDateTime.now());

    when(userFactory.createUser("user1")).thenReturn(new UUIDUser("user1"));
    when(shortenedLinksRepository.getLink("https://short.ly/x")).thenReturn(limited);
    when(ttlGlobalRepository.getTtl()).thenReturn(null);

    assertThrows(IllegalStateException.class, () -> algorithm.process(command));
    verify(shortenedLinksRepository).deleteLinkById(10);
  }

  @Test
  void process_throws_whenLinkBelongsToAnotherUser() {
    OpenLinkCommand command = makeCommand("userX", "https://short.ly/x");
    LinkDao linkDao =
        makeLinkDao(15, "userY", "https://example.com", 0, null, null, LocalDateTime.now());

    when(userFactory.createUser("userX")).thenReturn(new UUIDUser("userX"));
    when(shortenedLinksRepository.getLink("https://short.ly/x")).thenReturn(linkDao);

    assertThrows(IllegalStateException.class, () -> algorithm.process(command));
  }

  @Test
  void process_throws_whenDesktopBrowseFails() throws Exception {
    OpenLinkCommand command = makeCommand("user1", "https://short.ly/x");
    LinkDao linkDao =
        makeLinkDao(12, "user1", "https://baduri.com", 0, null, null, LocalDateTime.now());
    when(userFactory.createUser("user1")).thenReturn(new UUIDUser("user1"));
    when(shortenedLinksRepository.getLink("https://short.ly/x")).thenReturn(linkDao);

    Desktop desktop = mock(Desktop.class);
    doThrow(IOException.class).when(desktop).browse(any(URI.class));
    desktop.browse(any());

    assertThrows(IllegalStateException.class, () -> algorithm.process(command));
  }
}
