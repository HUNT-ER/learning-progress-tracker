package tracker.input;

import java.util.Scanner;

public class UserInputService {

  private Scanner scanner;

  public UserInputService() {
    scanner = new Scanner(System.in);
  }

  public String getStringInput() {
    if (scanner.hasNextLine()) {
      return scanner.nextLine().trim();
    }
    return "";
  }
}
