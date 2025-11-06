package anton.asmirko.app.model.commands;

public record TtlGlobalCommand(Integer ttl) implements CLICommand {
  public static final String STR_REP = "ttlGlobal";

  @Override
  public String getStrRep() {
    return STR_REP;
  }
}
