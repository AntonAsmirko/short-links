package anton.asmirko.app.service.shorten.alg;

import java.net.URI;

public interface LinkShorteningAlgorithm {
  String shorten(String uuid, URI link);

  String getName();
}
