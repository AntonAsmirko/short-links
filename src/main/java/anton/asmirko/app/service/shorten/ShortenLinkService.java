package anton.asmirko.app.service.shorten;

import anton.asmirko.app.service.shorten.alg.Base62ShorteningAlgorithm;
import anton.asmirko.app.service.shorten.alg.LinkShorteningAlgorithm;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShortenLinkService {

  private final Map<String, LinkShorteningAlgorithm> algorithms;

  public String shortenLink(final String uuid, final URI link) {
    final String algName = System.getProperty("app.shortening.alg");
    final LinkShorteningAlgorithm alg =
        algorithms.get(Objects.requireNonNullElse(algName, Base62ShorteningAlgorithm.ALG_NAME));
    return Optional.ofNullable(alg)
        .map(it -> it.shorten(uuid, link))
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format("Алгоритм сокращения ссылки %s не поддерживается", algName)));
  }
}
