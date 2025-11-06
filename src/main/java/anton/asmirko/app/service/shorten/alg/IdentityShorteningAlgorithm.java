package anton.asmirko.app.service.shorten.alg;

import java.net.URI;
import org.springframework.stereotype.Component;

@Component(IdentityShorteningAlgorithm.ALG_NAME)
public class IdentityShorteningAlgorithm implements LinkShorteningAlgorithm {
  static final String ALG_NAME = "identity";

  @Override
  public String shorten(final String uuid, final URI link) {
    return String.format("%s:%s", uuid, link);
  }

  @Override
  public String getName() {
    return ALG_NAME;
  }
}
