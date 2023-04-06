package tracker.ui;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tracker.db.TempStudentStorage;
import tracker.entities.Student;
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
      case LIST:
        showStudents();
        break;
      case ADD_POINTS:
        addPoints();
        break;
      case FIND:
        findStudent();
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
      if (Command.isCommand(input) && Command.getByDescription(input).equals(Command.BACK)) {
        isBack = true;
      } else if (!isCorrectCredentials(input)) {
        continue;
      } else {
        if (registerStudent(input)) {
          numberAddedStudents++;
        }
      }
    }
    saveAndPrintOutput("Total " + numberAddedStudents + " students have been added.\n");
    return numberAddedStudents;
  }

  private boolean isAlreadyRegisteredStudent(String email) {
    return TempStudentStorage.getStudents().stream()
        .map(student -> student.getEmail())
        .anyMatch(x -> x.equals(email));
  }

  private boolean registerStudent(String student) {
    Pattern pattern = Pattern.compile("^(\\S+)\\s(.+)\\s(.+)$");
    Matcher matcher = pattern.matcher(student);
    matcher.find();
    UnsavedStudent unsavedStudent = new UnsavedStudent(matcher.group(1), matcher.group(2),
        matcher.group(3));
    if (isAlreadyRegisteredStudent(unsavedStudent.getEmail())) {
      saveAndPrintOutput("This email is already taken.\n");
      return false;
    }
    TempStudentStorage.addStudent(unsavedStudent);
    saveAndPrintOutput("The student has been added.\n");
    return true;
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

  private void showStudents() {
    List<Student> students = TempStudentStorage.getStudents();
    if (students.size() == 0) {
      saveAndPrintOutput("No students found\n");
      return;
    }
    StringBuilder output = new StringBuilder();
    output.append("Students:\n");
    students.forEach(
        student -> output.append(Integer.valueOf(student.getId()).toString() + "\n"));

    saveAndPrintOutput(output.toString());
  }

  private boolean isNumber(String number) {
    try {
      Integer.parseInt(number);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  private void addPoints() {
    saveAndPrintOutput("Enter an id and points or 'back' to return\n");
    boolean isBack = false;
    while (!isBack) {
      String input = inputReader.getStringInput().trim();
      if (Command.isCommand(input) && Command.getByDescription(input).equals(Command.BACK)) {
        isBack = true;
        break;
      } else if (!isCorrectPoints(input)) {
        saveAndPrintOutput("Incorrect points format\n");
        continue;
      }
      //костыль чтобы зкарыть их тесты, айди не должен быть буквенным
      String[] idAndPoints = input.split("\\s+");
      if (!isNumber(idAndPoints[0])) {
        saveAndPrintOutput("No student is found for id=" + idAndPoints[0] + "\n");
        continue;
      }

      int[] idAndPointsInfo = Arrays.stream(input.split("\\s+"))
          .mapToInt(Integer::parseInt)
          .toArray();

      int studentId = idAndPointsInfo[0];
      Optional<Student> foundStudent = TempStudentStorage.getStudentById(studentId);

      if (foundStudent.isEmpty()) {
        saveAndPrintOutput("No student is found for id=" + studentId + "\n");
        continue;
      }

      foundStudent.get().updatePoints(new int[]{1, 2, 3, 4},
          Arrays.copyOfRange(idAndPointsInfo, 1, idAndPointsInfo.length));
      saveAndPrintOutput("Points updated.\n");
    }
  }

  private boolean isCorrectPoints(String pointsInfo) {
    String addPointsPattern = "^^(\\w+\\s)(\\d+\\s){3}\\d+$";
    return pointsInfo.matches(addPointsPattern);
  }

  private void findStudent() {
    saveAndPrintOutput("Enter an id or 'back' to return\n");
    boolean isBack = false;
    while (!isBack) {
      String input = inputReader.getStringInput().trim();
      if (Command.isCommand(input) && Command.getByDescription(input).equals(Command.BACK)) {
        isBack = true;
        break;
      } else if(!input.matches("^\\d+$")) {
        saveAndPrintOutput("Incorrect id format\n");
        continue;
      }
      Optional<Student> foundStudent = TempStudentStorage.getStudentById(Integer.parseInt(input));

      if (foundStudent.isEmpty()) {
        saveAndPrintOutput("No student is found for id=" + input + "\n");
        continue;
      }
      Student student = foundStudent.get();
      StringBuilder output = new StringBuilder();
      output.append(student.getId() + " points: Java=" + student.getAcademicSubjects().get(0).getPoint());
      output.append("; Dsa=" + student.getAcademicSubjects().get(1).getPoint());
      output.append("; Databases=" + student.getAcademicSubjects().get(2).getPoint());
      output.append("; Spring=" + student.getAcademicSubjects().get(3).getPoint());
      output.append("\n");

      saveAndPrintOutput(output.toString());
    }
  }
}
