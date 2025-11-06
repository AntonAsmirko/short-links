package anton.asmirko.app.processing.algorithms;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import anton.asmirko.app.model.commands.TtlGlobalCommand;
import anton.asmirko.app.model.user.AdminUser;
import anton.asmirko.app.model.user.UUIDUser;
import anton.asmirko.app.model.user.UserFactory;
import anton.asmirko.app.repository.ttl.TtlGlobalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TtlGlobalCommandProcessingAlgTest {

  private TtlGlobalRepository ttlGlobalRepository;
  private UserFactory userFactory;
  private TtlGlobalCommandProcessingAlg algorithm;

  @BeforeEach
  void setUp() {
    ttlGlobalRepository = mock(TtlGlobalRepository.class);
    userFactory = mock(UserFactory.class);
    algorithm = new TtlGlobalCommandProcessingAlg(ttlGlobalRepository, userFactory);
  }

  @Test
  void process_setsTtl_whenPositiveValueProvided() {
    var cmd = new TtlGlobalCommand(3600);
    AdminUser admin = spy(new AdminUser("root"));
    doNothing().when(admin).askAuth();

    when(userFactory.createUser("admin")).thenReturn(admin);

    algorithm.process(cmd);

    verify(ttlGlobalRepository).setTtl(3600);
    verify(admin).askAuth();
  }

  @Test
  void process_throws_whenNegativeTtlProvided() {
    var cmd = new TtlGlobalCommand(-5);
    when(userFactory.createUser("admin")).thenReturn(new UUIDUser("admin"));

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> algorithm.process(cmd));
    assertTrue(ex.getMessage().contains("меньше 0"));
    verify(ttlGlobalRepository, never()).setTtl(anyInt());
  }

  @Test
  void process_readsCurrentTtl_whenNullProvided() {
    var cmd = new TtlGlobalCommand(null);
    when(userFactory.createUser("admin")).thenReturn(new UUIDUser("admin"));
    when(ttlGlobalRepository.getTtl()).thenReturn(1234);

    algorithm.process(cmd);

    verify(ttlGlobalRepository).getTtl();
    verify(ttlGlobalRepository, never()).setTtl(anyInt());
  }

  @Test
  void process_callsAskAuth_whenAdminUser() {
    var cmd = new TtlGlobalCommand(100);
    AdminUser admin = spy(new AdminUser("root"));
    doNothing().when(admin).askAuth();

    when(userFactory.createUser("admin")).thenReturn(admin);

    algorithm.process(cmd);

    verify(admin).askAuth();
  }

  @Test
  void process_doesNotThrow_whenNormalUser() {
    var cmd = new TtlGlobalCommand(500);
    when(userFactory.createUser("admin")).thenReturn(new UUIDUser("admin"));

    assertDoesNotThrow(() -> algorithm.process(cmd));
    verify(ttlGlobalRepository).setTtl(500);
  }
}
