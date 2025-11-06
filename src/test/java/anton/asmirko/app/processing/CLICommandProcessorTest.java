package anton.asmirko.app.processing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import anton.asmirko.app.model.commands.CLICommand;
import anton.asmirko.app.processing.algorithms.CLICommandProcessingAlg;
import anton.asmirko.app.processing.exception.UnsupportedCommandException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CLICommandProcessorTest {

  private CLICommandProcessingAlg<CLICommand> mockAlg;
  private CLICommandProcessor processor;

  @BeforeEach
  void setUp() {
    mockAlg = mock(CLICommandProcessingAlg.class);
    processor = new CLICommandProcessor(Map.of("shorten", mockAlg));
  }

  private CLICommand makeCommand(String name) {
    CLICommand cmd = mock(CLICommand.class);
    when(cmd.getStrRep()).thenReturn(name);
    return cmd;
  }

  @Test
  void process_invokesAlgorithm_whenCommandSupported() {
    CLICommand cmd = makeCommand("shorten");

    processor.process(cmd);

    verify(mockAlg).process(cmd);
  }

  @Test
  void process_throwsException_whenCommandUnsupported() {
    CLICommand cmd = makeCommand("unknown");

    UnsupportedCommandException ex =
        assertThrows(UnsupportedCommandException.class, () -> processor.process(cmd));

    assertTrue(ex.getMessage().contains("Команда unknown"));
    verify(mockAlg, never()).process(any());
  }

  @Test
  void process_doesNotThrow_whenAlgorithmMapContainsMultipleEntries() {
    var anotherAlg = mock(CLICommandProcessingAlg.class);
    processor =
        new CLICommandProcessor(
            Map.of(
                "shorten", mockAlg,
                "open", anotherAlg));
    CLICommand cmd = makeCommand("open");

    assertDoesNotThrow(() -> processor.process(cmd));
    verify(anotherAlg).process(cmd);
  }
}
