package tracker.ui;

import java.util.StringJoiner;
import tracker.enums.Command;
import tracker.input.UserInputService;
import tracker.session.Session;

public abstract class CommandLineUi {

  protected UserInputService inputReader;
  protected Session session;

  protected StringJoiner consoleOutput;

  protected CommandLineUi(Session session) {
    this.session = session;
    this.inputReader = new UserInputService();
    consoleOutput = new StringJoiner("\n");
  }

  public String showUi() {
    consoleOutput = new StringJoiner("\n");
    processInput(inputReader.getStringInput());
    return consoleOutput.toString();
  }

  public abstract void processInput(String input);

  public StringJoiner getConsoleOutput() {
    return consoleOutput;
  }

  public void setInputReader(UserInputService inputReader) {
    this.inputReader = inputReader;
  }


}
