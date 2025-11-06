package anton.asmirko.app.service.shorten.alg;

import anton.asmirko.app.repository.linksseq.LinksSequenceRepository;
import anton.asmirko.app.utils.Base62;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component(Base62ShorteningAlgorithm.ALG_NAME)
@RequiredArgsConstructor
public class Base62ShorteningAlgorithm implements LinkShorteningAlgorithm {
  public static final String ALG_NAME = "base62";

  private final LinksSequenceRepository linksSequenceRepository;

  @Override
  public String shorten(String uuid, URI link) {
    final int seed = linksSequenceRepository.getNext();
    return Base62.encode(seed);
  }

  @Override
  public String getName() {
    return ALG_NAME;
  }
}
