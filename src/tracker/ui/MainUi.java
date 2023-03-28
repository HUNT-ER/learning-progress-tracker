package tracker.ui;

import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tracker.db.TempStudentStorage;
import tracker.entities.UnsavedStudent;
import tracker.enums.Command;
import tracker.session.Session;

public class MainUi extends CommandLineUi {

  public MainUi(Session session) {
    super(session);
  }

  @Override
  public void processInput(String input) {
    if ("".equals(input)) {
      saveAndPrintOutput("No input.\n");
    } else if (!Command.isCommand(input)) {
      saveAndPrintOutput("Error: unknown command!\n");
    } else {
      processCommand(Command.getByDescription(input));
    }
  }

  public void processCommand(Command command) {
    switch (command) {
      case EXIT:
        session.close();
        saveAndPrintOutput("Bye!");
        break;
      case ADD_STUDENTS:
        addStudents();
        break;
      default:
        saveAndPrintOutput("Enter 'exit' to exit the program.\n");
        break;
    }
  }

  private void saveAndPrintOutput(String output) {
    consoleOutput.add(output);
    System.out.print(output);
  }

  private int addStudents() {
    saveAndPrintOutput("Enter student credentials or 'back' to return:\n");
    int numberAddedStudents = 0;
    boolean isBack = false;
    while (!isBack) {
      String input = inputReader.getStringInput().trim();
      //проверяем, если ввод является командой и если это команда BACK, то выходим из процесса
      if (Command.isCommand(input) && Command.valueOf(input.toUpperCase()).equals(Command.BACK)) {
        isBack = true;
      } else if (!isCorrectCredentials(input)) {
        continue;
      } else {
        registerStudent(input);
        numberAddedStudents++;
      }
    }
    saveAndPrintOutput("Total " + numberAddedStudents + " students have been added.\n");
    return numberAddedStudents;
  }

  private void registerStudent(String student) {
    Pattern pattern = Pattern.compile("^(\\S+)\\s(.+)\\s(.+)$");
    Matcher matcher = pattern.matcher(student);
    matcher.find();
    UnsavedStudent unsavedStudent = new UnsavedStudent(matcher.group(1), matcher.group(2),
        matcher.group(3));
    TempStudentStorage.addStudent(unsavedStudent);
    saveAndPrintOutput("The student has been added.\n");
  }

  private boolean isCorrectCredentials(String credentials) {
    String credentialsPattern = "^(\\S+)\\s(.+)\\s(.+)$";
    Pattern pattern = Pattern.compile(credentialsPattern);
    Matcher matcher = pattern.matcher(credentials);
    if (!matcher.find()) {
      saveAndPrintOutput("Incorrect credentials.\n");
      return false;
    }
    if (!matcher.group(1).trim().matches("\\w+[\\-']?\\w+")) {
      saveAndPrintOutput("Incorrect first name.\n");
      return false;
    }
    if (!matcher.group(2).trim().matches("(\\w+[\\-']?\\w+([\\-']?\\w+)?\\s?)+")) {
      saveAndPrintOutput("Incorrect last name.\n");
      return false;
    }
    if (!matcher.group(3).trim().matches("[\\w\\.]+@\\w+\\.\\w+")) {
      saveAndPrintOutput("Incorrect email.\n");
      return false;
    }
    return true;
  }
}
