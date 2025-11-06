package anton.asmirko.app.service.shorten.alg;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import anton.asmirko.app.repository.linksseq.LinksSequenceRepository;
import anton.asmirko.app.utils.Base62;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class Base62ShorteningAlgorithmTest {

  private LinksSequenceRepository repository;
  private Base62ShorteningAlgorithm algorithm;

  @BeforeEach
  void setUp() {
    repository = Mockito.mock(LinksSequenceRepository.class);
    algorithm = new Base62ShorteningAlgorithm(repository);
  }

  @Test
  void shorten_returnsBase62EncodedValueOfSeed() {
    when(repository.getNext()).thenReturn(125);
    String result = algorithm.shorten("some-uuid", URI.create("https://example.com"));
    assertEquals(Base62.encode(125), result);
    verify(repository, times(1)).getNext();
  }

  @Test
  void shorten_returnsDifferentValuesForDifferentSeeds() {
    when(repository.getNext()).thenReturn(1, 2, 3);
    String r1 = algorithm.shorten("uuid", URI.create("https://a.com"));
    String r2 = algorithm.shorten("uuid", URI.create("https://b.com"));
    String r3 = algorithm.shorten("uuid", URI.create("https://c.com"));
    assertNotEquals(r1, r2);
    assertNotEquals(r2, r3);
  }

  @Test
  void getName_returnsConstantAlgorithmName() {
    assertEquals("base62", algorithm.getName());
  }
}
