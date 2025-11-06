package anton.asmirko.app.CLI;

import static org.junit.jupiter.api.Assertions.*;

import anton.asmirko.app.model.commands.*;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CLIReaderTest {

  private CLIReader cliReader;

  @BeforeEach
  void setUp() {
    cliReader = new CLIReader();
  }

  @Test
  void returnsHelpCommand_whenNoArgumentsProvided() {
    CLICommand cmd = cliReader.readArgs(new String[] {});
    assertInstanceOf(HelpCommand.class, cmd);
  }

  @Test
  void parsesShortenCommand_whenValidUriProvided() {
    String uri = "https://example.com";
    CLICommand cmd = cliReader.readArgs(new String[] {"shorten", uri});
    assertInstanceOf(ShortenLinkCommand.class, cmd);
    ShortenLinkCommand shorten = (ShortenLinkCommand) cmd;
    assertEquals(URI.create(uri), shorten.uri());
    assertTrue(shorten.keys().isEmpty());
  }

  @Test
  void returnsHelpCommand_whenShortenCommandWithoutUri() {
    assertEquals(new HelpCommand(), cliReader.readArgs(new String[] {"shorten"}));
  }

  @Test
  void returnsHelpCommand_whenUnknownCommandProvided() {
    CLICommand cmd = cliReader.readArgs(new String[] {"unknown"});
    assertInstanceOf(HelpCommand.class, cmd);
  }

  @Test
  void parsesOpenCommand_whenUriProvided() {
    String uri = "https://site.org";
    CLICommand cmd = cliReader.readArgs(new String[] {"open", uri});
    assertInstanceOf(OpenLinkCommand.class, cmd);
    OpenLinkCommand open = (OpenLinkCommand) cmd;
    assertEquals(uri, open.uri());
  }

  @Test
  void returnsHelpCommand_whenOpenCommandWithoutUri() {
    assertEquals(new HelpCommand(), cliReader.readArgs(new String[] {"open"}));
  }

  @Test
  void parsesTtlGlobalCommand_whenValueProvided() {
    CLICommand cmd = cliReader.readArgs(new String[] {"ttlGlobal", "3600"});
    assertInstanceOf(TtlGlobalCommand.class, cmd);
    TtlGlobalCommand ttl = (TtlGlobalCommand) cmd;
    assertEquals(3600, ttl.ttl());
  }

  @Test
  void parsesTtlGlobalCommand_whenDifferentNumericValueProvided() {
    CLICommand cmd = cliReader.readArgs(new String[] {"ttlGlobal", "2"});
    assertInstanceOf(TtlGlobalCommand.class, cmd);
    TtlGlobalCommand ttl = (TtlGlobalCommand) cmd;
    assertEquals(2, ttl.ttl());
  }

  @Test
  void throwsIllegalArgumentException_whenPruneCommandWithoutArguments() {
    assertThrows(IllegalArgumentException.class, () -> cliReader.readArgs(new String[] {"prune"}));
  }

  @Test
  void returnsHelpCommand_whenShortenCommandWithInvalidUri() {
    assertEquals(new HelpCommand(), cliReader.readArgs(new String[] {"shorten", "not_a_url"}));
  }

  @Test
  void returnsHelpCommand_whenShortenCommandWithUnsupportedScheme() {
    assertEquals(
        new HelpCommand(), cliReader.readArgs(new String[] {"shorten", "ftp://wrong.scheme"}));
  }

  @Test
  void doesNotThrowException_whenShortenCommandWithHttpOrHttpsUri() {
    assertDoesNotThrow(() -> cliReader.readArgs(new String[] {"shorten", "http://test.com"}));
    assertDoesNotThrow(() -> cliReader.readArgs(new String[] {"shorten", "https://test.com"}));
  }
}
