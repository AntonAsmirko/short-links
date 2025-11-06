package anton.asmirko.app.model.commands;

public record HelpCommand() implements CLICommand {
  public static final String STR_REP = "help";

  @Override
  public String getStrRep() {
    return STR_REP;
  }
}
