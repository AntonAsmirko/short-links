package anton.asmirko.app.service.shorten.alg;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdentityShorteningAlgorithmTest {

  private IdentityShorteningAlgorithm algorithm;

  @BeforeEach
  void setUp() {
    algorithm = new IdentityShorteningAlgorithm();
  }

  @Test
  void shorten_returnsUuidAndUriSeparatedByColon() {
    String uuid = "123e4567-e89b-12d3-a456-426614174000";
    URI link = URI.create("https://example.com");
    String result = algorithm.shorten(uuid, link);
    assertEquals(uuid + ":" + link.toString(), result);
  }

  @Test
  void shorten_handlesDifferentLinksCorrectly() {
    String uuid = "abc";
    URI first = URI.create("https://first.com");
    URI second = URI.create("https://second.com");
    String r1 = algorithm.shorten(uuid, first);
    String r2 = algorithm.shorten(uuid, second);
    assertNotEquals(r1, r2);
  }

  @Test
  void shorten_includesUuidEvenIfEmpty() {
    URI link = URI.create("https://example.org");
    String result = algorithm.shorten("", link);
    assertEquals(":" + link.toString(), result);
  }

  @Test
  void shorten_handlesUriWithQueryAndFragment() {
    String uuid = "id";
    URI link = URI.create("https://example.com/page?x=1#top");
    String result = algorithm.shorten(uuid, link);
    assertEquals("id:" + link.toString(), result);
  }

  @Test
  void getName_returnsConstantAlgorithmName() {
    assertEquals("identity", algorithm.getName());
  }
}
