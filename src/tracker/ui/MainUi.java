package tracker.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import tracker.db.TempStudentStorage;
import tracker.entities.Student;
import tracker.entities.UnsavedStudent;
import tracker.entities.subjects.AcademicSubject;
import tracker.enums.Activity;
import tracker.enums.Command;
import tracker.enums.Course;
import tracker.enums.Difficult;
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
      case STATISTICS:
        getStatistics();
        break;
      case NOTIFY:
        notifyStudents();
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
        .map(Student::getEmail)
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
      } else if (!input.matches("^\\d+$")) {
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
      output.append(
          student.getId() + " points: Java=" + student.getAcademicSubjects().get(0).getPoint());
      output.append("; Dsa=" + student.getAcademicSubjects().get(1).getPoint());
      output.append("; Databases=" + student.getAcademicSubjects().get(2).getPoint());
      output.append("; Spring=" + student.getAcademicSubjects().get(3).getPoint());
      output.append("\n");

      saveAndPrintOutput(output.toString());
    }
  }

  private void getStatistics() {
    saveAndPrintOutput("Type the name of a course to see details or 'back' to quit:\n");
    printStatistics();
    boolean isBack = false;
    while (!isBack) {
      String input = inputReader.getStringInput().trim();
      if (Command.isCommand(input) && Command.getByDescription(input).equals(Command.BACK)) {
        isBack = true;
        break;
      } else if (!Course.isExistedCourse(input)) {
        saveAndPrintOutput("Unknown course\n");
        continue;
      }
      saveAndPrintOutput(showSubjectStatistics(input) + "\n");
    }
  }

  private void printStatistics() {
    String mostPopular = getMostPopularSubject();
    String leastPopular = getLeastPopularSubject();
    String highestActivity = getRankedActivitySubject(Activity.HIGHEST);
    String lowestActivity = getRankedActivitySubject(Activity.LOWEST);
    String easiest = getRankedDifficultSubject(Difficult.EASY);
    String hardest = getRankedDifficultSubject(Difficult.HARD);

    if (leastPopular.equals(mostPopular)) {
      leastPopular = "n/a";
    }

    saveAndPrintOutput(
        showSubjectsStatistics(mostPopular, leastPopular, highestActivity, lowestActivity, easiest,
            hardest) + "\n");
  }

  private String showSubjectsStatistics(String mostPopular, String leastPopular,
      String highestActivity, String lowestActivity, String easiest, String hardest) {

    StringJoiner output = new StringJoiner("\n");
    output.add("Most popular: " + mostPopular);
    output.add("Least  popular: " + leastPopular);
    output.add("Highest activity: " + highestActivity);
    output.add("Lowest activity: " + lowestActivity);
    output.add("Easiest course: " + easiest);
    output.add("Hardest course: " + hardest);
    return output.toString();
  }

  private List<Map.Entry<String, Long>> getRankedSubjectList(List<Student> students,
      Predicate<AcademicSubject> subjectFilter, Collector<AcademicSubject, ?, Long> groupingSign) {
    return students.stream()
        .flatMap(student -> student.getAcademicSubjects().stream())
        .filter(subjectFilter)
        //группируем как предмет-количество ч-к
        .collect(Collectors.groupingBy(AcademicSubject::getName, groupingSign))
        .entrySet()
        .stream()
        //сортируем по значению
        .sorted(Entry.<String, Long>comparingByValue().reversed()
            .thenComparing(Entry.comparingByKey()))
        .collect(Collectors.toList());
  }


  private List<String> getRankedSubjectListByAveragePoints(List<Student> students,
      Predicate<AcademicSubject> subjectFilter,
      Collector<AcademicSubject, ?, Double> groupingSign) {
    return students.stream()
        .flatMap(student -> student.getAcademicSubjects().stream())
        .filter(subjectFilter)
        //группируем как предмет-количество ч-к
        .collect(Collectors.groupingBy(AcademicSubject::getName, groupingSign))
        .entrySet()
        .stream()
        //сортируем по значению
        .sorted(Entry.<String, Double>comparingByValue().reversed()
            .thenComparing(Entry.comparingByKey()))
        .map(Entry::getKey)
        .collect(Collectors.toList());
  }

  private String getMostPopularSubject() {
    StringJoiner mostPopular = new StringJoiner(", ");
    List<Map.Entry<String, Long>> popularSubjects = getRankedSubjectList(
        TempStudentStorage.getStudents(), academicSubject -> academicSubject.getPoint() > 0,
        Collectors.counting());

    if (popularSubjects.size() == 0) {
      mostPopular.add("n/a");
    } else {
      long mostPopularNumber = popularSubjects.get(0).getValue();
      popularSubjects.stream().filter(x -> x.getValue() == mostPopularNumber)
          .forEach(x -> mostPopular.add(x.getKey()));
    }
    return mostPopular.toString();
  }

  private String getLeastPopularSubject() {
    StringJoiner output = new StringJoiner(", ");
    List<Map.Entry<String, Long>> rankedSubjects = getRankedSubjectList(
        TempStudentStorage.getStudents(), academicSubject -> academicSubject.getPoint() > 0,
        Collectors.counting());
    if (rankedSubjects.size() == 0) {
      output.add("n/a");
    } else if (rankedSubjects.size() != 4) {
      List<String> allSubjects = Arrays.stream(Course.nameValues()).collect(Collectors.toList());
      allSubjects.removeAll(
          rankedSubjects.stream().map(Entry::getKey).collect(Collectors.toList()));
      allSubjects.forEach(x -> output.add(x));
      //если не 4, то искомое = Все курсы - всё из ранкеда
    } else {
      long leastPopularNumber = rankedSubjects.get(rankedSubjects.size() - 1).getValue();
      List<String> leastPopularSubjects = rankedSubjects.stream()
          .filter(x -> x.getValue() == leastPopularNumber).map(Entry::getKey)
          .collect(Collectors.toList());
      leastPopularSubjects.forEach(x -> output.add(x));
    }
    return output.toString().equals(getMostPopularSubject()) ? "n/a" : output.toString();
  }

  private String getRankedActivitySubject(Activity activity) {
    List<Map.Entry<String, Long>> rankedSubjectsList = getRankedSubjectList(
        TempStudentStorage.getStudents(),
        subject -> true, Collectors.summingLong(AcademicSubject::getPoint));
    if (TempStudentStorage.getStudents().stream().filter(student -> student.isEnrolled()).count()
        == 0) {
      return "n/a";
    }
    if (activity == Activity.LOWEST) {
      return rankedSubjectsList.get(rankedSubjectsList.size() - 1).getKey();
    } else {

      return rankedSubjectsList.get(0).getKey();
    }
  }

  private String getRankedDifficultSubject(Difficult difficult) {
    List<String> rankedSubjectsList = getRankedSubjectListByAveragePoints(
        TempStudentStorage.getStudents(), subject -> true,
        Collectors.averagingLong(AcademicSubject::getPoint));
    if (TempStudentStorage.getStudents().stream().filter(student -> student.isEnrolled()).count()
        == 0) {
      return "n/a";
    }
    if (difficult == Difficult.EASY) {
      return rankedSubjectsList.get(0);
    } else {
      return rankedSubjectsList.get(rankedSubjectsList.size() - 1);
    }
  }

  //Перед использованием убедиться, что имя предмета корректное
  private String showSubjectStatistics(String subjectName) {
    List<String[]> studentStats = new ArrayList<>();
    TempStudentStorage.getStudents()
        .stream()
        .forEach(student -> {
          String id = Integer.toString(student.getId());
          String[] stats = student.getAcademicSubjectStats(subjectName);
          studentStats.add(new String[]{id, stats[1], stats[2]});
        });
    Comparator<String[]> comp = Comparator.<String[], Integer>comparing(
            stat -> Integer.parseInt(stat[1])).reversed()
        .thenComparing(stat -> Integer.parseInt(stat[1]));
    Comparator<String[]> comp2 = comp;
    Comparator<String[]> comps = Comparator.<String[], String>comparing(arr -> arr[1]).reversed()
        .thenComparing(arr -> arr[1]);
    studentStats.sort(comp);

    StringJoiner output = new StringJoiner("\n");
    output.add(Course.getCourseByName(subjectName).getName());
    output.add(String.format("%-5s %-9s %-8s", "id", "points", "completed"));
    studentStats.stream().filter(x -> !"0".equals(x[1]))
        .forEach(arr -> output.add(String.format("%-5s %-9s %s", arr[0], arr[1], arr[2])));
    return output.toString();
  }

  private void notifyStudents() {
    saveAndPrintOutput(setNotification(getUnnotifiedStudents(TempStudentStorage.getStudents())));
  }

  private Map<Student, List<AcademicSubject>> getUnnotifiedStudents(List<Student> students) {
    return students.stream()
        .filter(Student::isEnrolled)
        .collect(
            Collectors.toMap(student -> student, student -> student.getAcademicSubjects().stream()
                .filter(subject -> !subject.isNotified() && subject.getPoint() > 0).collect(
                    Collectors.toList())));
  }

  private String setNotification(Map<Student, List<AcademicSubject>> unnotifiedStudents) {
    StringBuilder notification = new StringBuilder();
    AtomicInteger counter = new AtomicInteger(0);
    unnotifiedStudents.forEach((k, v) -> {
          if (v.size() != 0) {
            counter.getAndIncrement();
          }
          v.forEach(subject -> {
            notification.append("To: ");
            notification.append(k.getEmail());
            notification.append("\n");
            notification.append("Re: Your Learning Progress");
            notification.append("\n");
            notification.append("Hello, ");
            notification.append(k.getName());
            notification.append(" ");
            notification.append(k.getLastname());
            notification.append("! You have accomplished our ");
            notification.append(subject.getName());
            notification.append(" course!\n");
            subject.setNotify();
          });
        }
    );
    notification.append("Total ");
    notification.append(unnotifiedStudents.isEmpty() ? 0 : counter);
    notification.append(" students have been notified.\n");

    return notification.toString();
  }
}
