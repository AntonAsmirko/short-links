package anton.asmirko.app.service.user.user;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UUIDService {

  public String getUUID() {
    return UUID.randomUUID().toString();
  }
}
