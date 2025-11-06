package anton.asmirko.app.processing.algorithms;

import anton.asmirko.app.model.commands.CLICommand;

public interface CLICommandProcessingAlg<T extends CLICommand> {
  void process(T cliCommand);
}
