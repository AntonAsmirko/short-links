package anton.asmirko.app;

import anton.asmirko.app.CLI.CLIReader;
import anton.asmirko.app.model.commands.CLICommand;
import anton.asmirko.app.processing.CLICommandProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShortLinksApp {

  private final CLIReader cliReader;
  private final CLICommandProcessor processor;

  public void run(final String[] args) {
    try {
      final CLICommand command = cliReader.readArgs(args);
      processor.process(command);
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}
