package anton.asmirko.app.service.shorten;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import anton.asmirko.app.service.shorten.alg.Base62ShorteningAlgorithm;
import anton.asmirko.app.service.shorten.alg.LinkShorteningAlgorithm;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ShortenLinkServiceTest {

  private Map<String, LinkShorteningAlgorithm> algorithms;
  private LinkShorteningAlgorithm base62Mock;
  private LinkShorteningAlgorithm identityMock;
  private ShortenLinkService service;

  @BeforeEach
  void setUp() {
    base62Mock = Mockito.mock(LinkShorteningAlgorithm.class);
    identityMock = Mockito.mock(LinkShorteningAlgorithm.class);
    algorithms = new HashMap<>();
    algorithms.put(Base62ShorteningAlgorithm.ALG_NAME, base62Mock);
    algorithms.put("identity", identityMock);
    service = new ShortenLinkService(algorithms);
  }

  @AfterEach
  void tearDown() {
    System.clearProperty("app.shortening.alg");
  }

  @Test
  void shortenLink_usesBase62ByDefault_whenPropertyNotSet() {
    URI uri = URI.create("https://example.com");
    when(base62Mock.shorten("uuid", uri)).thenReturn("short1");

    String result = service.shortenLink("uuid", uri);

    assertEquals("short1", result);
    verify(base62Mock, times(1)).shorten("uuid", uri);
    verify(identityMock, never()).shorten(any(), any());
  }

  @Test
  void shortenLink_usesAlgorithmDefinedInSystemProperty() {
    System.setProperty("app.shortening.alg", "identity");
    URI uri = URI.create("https://site.org");
    when(identityMock.shorten("uuid", uri)).thenReturn("id:short");

    String result = service.shortenLink("uuid", uri);

    assertEquals("id:short", result);
    verify(identityMock, times(1)).shorten("uuid", uri);
    verify(base62Mock, never()).shorten(any(), any());
  }

  @Test
  void shortenLink_throwsException_whenAlgorithmNotFound() {
    System.setProperty("app.shortening.alg", "nonexistent");
    URI uri = URI.create("https://test.com");

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> service.shortenLink("uuid", uri));

    assertTrue(ex.getMessage().contains("Алгоритм сокращения ссылки"));
  }

  @Test
  void shortenLink_passesCorrectArgumentsToAlgorithm() {
    URI uri = URI.create("https://a.com/x");
    when(base62Mock.shorten("abc", uri)).thenReturn("encoded");

    String result = service.shortenLink("abc", uri);

    assertEquals("encoded", result);
    verify(base62Mock).shorten("abc", uri);
  }
}
