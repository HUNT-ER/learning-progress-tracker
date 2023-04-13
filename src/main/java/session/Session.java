package session;

import ui.CommandLineUi;
import ui.MainUi;

public class Session {

  private CommandLineUi ui;
  private boolean isClosed;

  public Session() {
    this.ui = new MainUi(this);
    isClosed = false;
  }

  public void start() {
    printTitle();
    do {
      ui.showUi();
    } while (!isClosed);
  }

  public void close() {
    isClosed = true;
  }

  public CommandLineUi getUi() {
    return ui;
  }

  public boolean isClosed() {
    return isClosed;
  }

  private String printTitle() {
    String title = "Learning Progress Tracker\n";
    System.out.print(title);
    return title;
  }
}
