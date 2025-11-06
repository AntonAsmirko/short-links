package anton.asmirko.app.processing;

import anton.asmirko.app.model.commands.CLICommand;
import anton.asmirko.app.processing.algorithms.CLICommandProcessingAlg;
import anton.asmirko.app.processing.exception.UnsupportedCommandException;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CLICommandProcessor {

  private final Map<String, CLICommandProcessingAlg<?>> algorithms;

  @SuppressWarnings("unchecked")
  public void process(CLICommand cliCommand) {
    var alg = (CLICommandProcessingAlg<CLICommand>) algorithms.get(cliCommand.getStrRep());
    Optional.ofNullable(alg)
        .orElseThrow(
            () ->
                new UnsupportedCommandException(
                    String.format("Команда %s не поддердивается", cliCommand.getStrRep())))
        .process(cliCommand);
  }
}
