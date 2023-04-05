package tracker.enums;

import java.util.Arrays;

public enum Command {
  EXIT("exit"), ADD_STUDENTS("add students"), BACK("back"), UNKNOWN("unknown"), LIST(
      "list"), ADD_POINTS("add points"), FIND("find");// QUIT, HELP;
  private String description;

  Command(String description) {
    this.description = description;
  }

  public static Command getByDescription(String description) {
    if (!isCommand(description)) {
      return UNKNOWN;
    } else {
      return Arrays.stream(Command.values())
          .filter(command -> command.description.equalsIgnoreCase(description))
          .findFirst().get();
    }
  }

  public static boolean isCommand(String input) {
    return Arrays.stream(Command.values())
        .anyMatch(command -> command.description.equalsIgnoreCase(input));
  }
}
