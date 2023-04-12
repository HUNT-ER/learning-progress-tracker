package tracker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import tracker.db.TempStudentStorage;
import tracker.entities.Student;
import tracker.entities.UnsavedStudent;
import tracker.entities.subjects.AcademicSubject;
import tracker.enums.Activity;
import tracker.enums.Difficult;
import tracker.input.UserInputService;
import tracker.session.Session;
import tracker.ui.MainUi;

public class MainUiTest {

  private static final InputStream SYSTEM_IN = System.in;
  private static final PrintStream SYSTEM_OUT = System.out;
  ByteArrayOutputStream out;
  private Session session;
  private MainUi mainUi;

  private void setOutput() {
    out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
  }

  private String getOutput() {
    return out.toString();
  }

  private void provideInput(String input) {
    ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
    System.setIn(in);
    mainUi.setInputReader(new UserInputService());
  }

  @BeforeEach
  public void setStartMenu() {
    session = new Session();
    mainUi = new MainUi(session);
  }

  @AfterEach
  public void restoreSystemIOStreams() {
    System.setIn(SYSTEM_IN);
    System.setOut(SYSTEM_OUT);
  }

  @AfterEach
  public void restoreStudentsStorage() throws NoSuchFieldException, IllegalAccessException {
    TempStudentStorage.setStudents(new ArrayList<>());
    Field studentsId = TempStudentStorage.class.getDeclaredField("studentsId");
    studentsId.setAccessible(true);
    ((AtomicInteger) studentsId.get(TempStudentStorage.class)).set(1);
  }

  @Test
  public void shouldPrintOnConsoleGivenString()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

    Method saveAndPrint = MainUi.class.getDeclaredMethod("saveAndPrintOutput", String.class);
    saveAndPrint.setAccessible(true);
    setOutput();
    saveAndPrint.invoke(mainUi, "Test message");
    String expectedOutput = "Test message";

    Assertions.assertEquals(expectedOutput, getOutput());
  }

  @Test
  public void consoleOutputShouldContainsGivenMessage()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

    Method saveAndPrint = MainUi.class.getDeclaredMethod("saveAndPrintOutput", String.class);
    saveAndPrint.setAccessible(true);
    saveAndPrint.invoke(mainUi, "Test message");
    String expectedOutput = "Test message";

    Assertions.assertEquals(expectedOutput, mainUi.getConsoleOutput().toString());
  }

  @Test
  public void shouldPrintNoInputMessage() {
    mainUi.processInput("");
    Assertions.assertEquals("No input.\n", mainUi.getConsoleOutput().toString());
  }

  @Test
  public void shouldPrintUnknownCommandMessage() {
    mainUi.processInput("wrong command");
    Assertions.assertEquals("Error: unknown command!\n", mainUi.getConsoleOutput().toString());
  }

  @Test
  public void shouldPrintCloseSessionAndProgram() {
    mainUi.processInput("exit");
    Assertions.assertEquals("Bye!", mainUi.getConsoleOutput().toString());
    Assertions.assertTrue(session.isClosed());
  }

  @Test
  public void shouldOpenAddPointsMenu() {
    provideInput("back");
    mainUi.processInput("add points");
    Assertions.assertEquals("Enter an id and points or 'back' to return\n",
        mainUi.getConsoleOutput().toString());
  }

  @Test
  public void shouldOpenFindStudentMenu() {
    provideInput("back");
    mainUi.processInput("find");
    Assertions.assertEquals("Enter an id or 'back' to return\n",
        mainUi.getConsoleOutput().toString());
  }

  @Test
  public void shouldBeTrueIfCredentialsIsCorrect()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    Method isCorrectCredentials = MainUi.class.getDeclaredMethod("isCorrectCredentials",
        String.class);
    isCorrectCredentials.setAccessible(true);

    String hasRegularCred = "Alex Hunter hunter@gmail.com";
    String hasSpaces = "Alex van der Last email@mail.ru";
    String hasHyphen = "Alex-Hunter Lastname vrio@yandex.ru";
    String hasHyphenAndSpaces = "Alex Meat-Hunter fast@gmail.com";

    Assertions.assertTrue((Boolean) isCorrectCredentials.invoke(mainUi, hasRegularCred));
    Assertions.assertTrue((Boolean) isCorrectCredentials.invoke(mainUi, hasSpaces));
    Assertions.assertTrue((Boolean) isCorrectCredentials.invoke(mainUi, hasHyphen));
    Assertions.assertTrue((Boolean) isCorrectCredentials.invoke(mainUi, hasHyphenAndSpaces));
  }

  @Test
  public void shouldBeFalseIfCredentialHasLessThenThreeWords()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    Method isCorrectCredentials = MainUi.class.getDeclaredMethod("isCorrectCredentials",
        String.class);
    isCorrectCredentials.setAccessible(true);

    String credential = "Alex Hunter";
    String credential2 = "Alex";
    String credential3 = "";

    Assertions.assertFalse((Boolean) isCorrectCredentials.invoke(mainUi, credential));
    Assertions.assertFalse((Boolean) isCorrectCredentials.invoke(mainUi, credential2));
    Assertions.assertFalse((Boolean) isCorrectCredentials.invoke(mainUi, credential3));

    String expectedOutput = "Incorrect credentials.\n\nIncorrect credentials.\n\nIncorrect credentials.\n";
    Assertions.assertEquals(expectedOutput, mainUi.getConsoleOutput().toString());

  }

  @Test
  public void shouldBeFalseIfNameHasOtherNotLiteralSymbol()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    Method isCorrectCredentials = MainUi.class.getDeclaredMethod("isCorrectCredentials",
        String.class);
    isCorrectCredentials.setAccessible(true);

    String credential = "'Alex Hunter hunter@gmail.com";
    String credential2 = "Alex- Hunter hunter@gmail.com";
    String credential3 = "x Hunter hunter@gmail.com";

    Assertions.assertFalse((Boolean) isCorrectCredentials.invoke(mainUi, credential));
    Assertions.assertFalse((Boolean) isCorrectCredentials.invoke(mainUi, credential2));
    Assertions.assertFalse((Boolean) isCorrectCredentials.invoke(mainUi, credential3));

    String expectedOutput = "Incorrect first name.\n\nIncorrect first name.\n\nIncorrect first name.\n";
    Assertions.assertEquals(expectedOutput, mainUi.getConsoleOutput().toString());

  }

  @Test
  public void shouldBeFalseIfLastNameHasOtherNotLiteralSymbol()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    Method isCorrectCredentials = MainUi.class.getDeclaredMethod("isCorrectCredentials",
        String.class);
    isCorrectCredentials.setAccessible(true);

    String credential = "Alex 'Hunter hunter@gmail.com";
    String credential2 = "Alex Hunter- hunter@gmail.com";
    String credential3 = "Alex Hun@ter hunter@gmail.com";

    Assertions.assertFalse((Boolean) isCorrectCredentials.invoke(mainUi, credential));
    Assertions.assertFalse((Boolean) isCorrectCredentials.invoke(mainUi, credential2));
    Assertions.assertFalse((Boolean) isCorrectCredentials.invoke(mainUi, credential3));

    String expectedOutput = "Incorrect last name.\n\nIncorrect last name.\n\nIncorrect last name.\n";
    Assertions.assertEquals(expectedOutput, mainUi.getConsoleOutput().toString());
  }

  @Test
  public void shouldBeFalseIfEmailHasOtherNotLiteralSymbol()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    Method isCorrectCredentials = MainUi.class.getDeclaredMethod("isCorrectCredentials",
        String.class);
    isCorrectCredentials.setAccessible(true);

    String credential = "Alex Hunter hunter$gmail.com";
    String credential2 = "Alex Hunter h@nter@gmail.com";
    String credential3 = "Alex Hunter hunte%r@gmail.com";

    Assertions.assertFalse((Boolean) isCorrectCredentials.invoke(mainUi, credential));
    Assertions.assertFalse((Boolean) isCorrectCredentials.invoke(mainUi, credential2));
    Assertions.assertFalse((Boolean) isCorrectCredentials.invoke(mainUi, credential3));

    String expectedOutput = "Incorrect email.\n\nIncorrect email.\n\nIncorrect email.\n";
    Assertions.assertEquals(expectedOutput, mainUi.getConsoleOutput().toString());
  }

  @Test
  public void shouldAddNewStudentsInStorage()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method registerStudent = MainUi.class.getDeclaredMethod("registerStudent", String.class);
    registerStudent.setAccessible(true);

    List<Student> students = TempStudentStorage.getStudents();
    //проверяем, чтобы хранилище было пустым
    Assertions.assertEquals(0, students.size());

    String testStudent = "Alex Hunter hunter@gmail.com";
    String testStudent2 = "Hunter Res res@gmail.com";
    //добавляем студента в хранилище
    registerStudent.invoke(mainUi, testStudent);
    //добавленный студент должен быть помещен в хранилище
    Assertions.assertEquals(1, students.size());
    registerStudent.invoke(mainUi, testStudent2);
    Assertions.assertEquals(2, students.size());
  }

  @ParameterizedTest
  @ValueSource(strings = {"Alex Hunter hunter@gmail.com"})
  public void tryToInsertStudentWithExistedEmail(String student)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method registerStudent = MainUi.class.getDeclaredMethod("registerStudent", String.class);
    registerStudent.setAccessible(true);
    TempStudentStorage.addStudent(new UnsavedStudent("Alex", "Hunter", "hunter@gmail.com"));
    //добавляем студента в хранилище
    registerStudent.invoke(mainUi, student);
    Assertions.assertEquals("This email is already taken.\n", mainUi.getConsoleOutput().toString());
  }

  @Test
  public void addedStudentShouldBeEqual()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method registerStudent = MainUi.class.getDeclaredMethod("registerStudent", String.class);
    registerStudent.setAccessible(true);

    String testStudent = "Alex Hunter hunter@gmail.com";
    //добавляем студента в хранилище
    registerStudent.invoke(mainUi, testStudent);

    List<Student> students = TempStudentStorage.getStudents();
    Student expectedStudent = new Student(1, "Alex", "Hunter", "hunter@gmail.com");
    Assertions.assertEquals(expectedStudent, students.get(0));
  }

  @Test
  public void shouldBePrintedAddStudentMessage()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method registerStudent = MainUi.class.getDeclaredMethod("registerStudent", String.class);
    registerStudent.setAccessible(true);
    String testStudent = "Alex Hunter hunter@gmail.com";
    //добавляем студента в хранилище
    registerStudent.invoke(mainUi, testStudent);
    Assertions.assertEquals("The student has been added.\n", mainUi.getConsoleOutput().toString());
  }

  @Test
  public void shouldPrintCorrectMessage()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

    Method addStudents = MainUi.class.getDeclaredMethod("addStudents");
    addStudents.setAccessible(true);

    String expectedOutput = "Enter student credentials or 'back' to return:\n\n"
        + "Total 0 students have been added.\n";
    provideInput("back");

    addStudents.invoke(mainUi);
    Assertions.assertEquals(expectedOutput, mainUi.getConsoleOutput().toString());
  }

  @Test
  public void shouldNotAddStudents()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

    Method addStudents = MainUi.class.getDeclaredMethod("addStudents");
    addStudents.setAccessible(true);
    provideInput("Alex Hunter 2000\r\nHunter email@yandex.ru\r\nback");

    Assertions.assertEquals(0, (int) addStudents.invoke(mainUi));
  }

  @Test
  public void shouldAddTwoStudents()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

    Method addStudents = MainUi.class.getDeclaredMethod("addStudents");
    addStudents.setAccessible(true);
    provideInput("Alex Hunter hunter@gmail.com\r\nHunter Alexio email@yandex.ru\r\nback");

    Assertions.assertEquals(2, (int) addStudents.invoke(mainUi));
  }

  @Test
  public void shouldGoToAddStudents() {
    provideInput("back\n");
    mainUi.processInput("add students");
    String expectedOutput = "Enter student credentials or 'back' to return:\n\n"
        + "Total 0 students have been added.\n";
    Assertions.assertEquals(expectedOutput, mainUi.getConsoleOutput().toString());
  }

  @Test
  public void shouldShowEmptyStudentsList() {
    mainUi.processInput("list");
    String expectedOutput = "No students found\n";
    Assertions.assertEquals(expectedOutput, mainUi.getConsoleOutput().toString());
  }

  @Test
  public void shouldShowStudentsList() {
    TempStudentStorage.addStudent(new UnsavedStudent("Alex", "Hunter", "hunter@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alex", "Hunterio", "hunter@mail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alex", "Huntern", "hunter@yandex.com"));
    mainUi.processInput("list");
    String expectedOutput = "Students:\n1\n2\n3\n";
    Assertions.assertEquals(expectedOutput, mainUi.getConsoleOutput().toString());
  }


  @ParameterizedTest
  @CsvSource({"'existed@gmail.com', true",
      "'existed@mail.ru', true",
      "'existed@yandex.ru', true",
      "'notexisted@gmail.com', false",
      "'notexistedemail@mail.ru', false",
      "'notexistedemail@yandex.ru', false"}
  )
  public void shouldCheckExistedEmail(String email, boolean isCorrect)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    TempStudentStorage.addStudent(new UnsavedStudent("First", "FirstLast", "existed@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Second", "SecondLast", "existed@mail.ru"));
    TempStudentStorage.addStudent(new UnsavedStudent("Third", "ThirdLast", "existed@yandex.ru"));

    Method isAlreadyRegisteredStudent = MainUi.class.getDeclaredMethod("isAlreadyRegisteredStudent",
        String.class);
    isAlreadyRegisteredStudent.setAccessible(true);
    Assertions.assertEquals(isCorrect, isAlreadyRegisteredStudent.invoke(mainUi, email));
  }

  @ParameterizedTest
  @CsvSource({
      "'1 1 2 1 1', true",
      "'1 4 3 2 1', true",
      "'s', false",
      "'', false",
      "'0 0 0', false",
      "'0 0', false",
      "'0', false",
      "'0', false",
      "'1 1 1 O', false",
      "'l 1 1 1 O', false"
  })
  public void isCorrectPointsShouldDeclineIncorrectInput(String input, boolean isCorrect)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method isCorrectPoints = MainUi.class.getDeclaredMethod("isCorrectPoints", String.class);
    isCorrectPoints.setAccessible(true);
    Assertions.assertEquals(isCorrect, isCorrectPoints.invoke(mainUi, input));
  }

  @ParameterizedTest
  @CsvSource({
      "'s s s s', 'Incorrect points format\n'",
      "'1 1 1 1', 'Incorrect points format\n'",
      "'10 10- 10 s', 'Incorrect points format\n'",
      "'10 10 10 10 10', 'No student is found for id=10\n'",
      "'15 10 10 10 10', 'No student is found for id=15\n'",
      "'40000 10 10 10 10', 'No student is found for id=40000\n'"
  })
  public void shouldPrintDifferentMessages(String input, String message)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    provideInput(input + "\nback\n");
    StringBuilder expectedOutput = new StringBuilder();
    expectedOutput.append("Enter an id and points or 'back' to return\n\n");
    expectedOutput.append(message);

    Method addStudents = MainUi.class.getDeclaredMethod("addPoints");
    addStudents.setAccessible(true);
    addStudents.invoke(mainUi);

    Assertions.assertEquals(expectedOutput.toString(), mainUi.getConsoleOutput().toString());
  }

  @Test
  public void shouldReturnBackFromAddPoints()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    provideInput("back\n");
    StringBuilder expectedOutput = new StringBuilder();
    expectedOutput.append("Enter an id and points or 'back' to return\n");

    Method addStudents = MainUi.class.getDeclaredMethod("addPoints");
    addStudents.setAccessible(true);
    addStudents.invoke(mainUi);

    Assertions.assertEquals(expectedOutput.toString(), mainUi.getConsoleOutput().toString());
  }

  @ParameterizedTest
  @CsvSource({
      "'1 1 1 1 1'",
      "'2 2 2 2 2'",
      "'3 3 3 3 3'"
  })
  public void shouldUpdateStudentPoints(String input)
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    provideInput(input + "\nback\n");
    TempStudentStorage.addStudent(new UnsavedStudent("Alex", "Hunter", "hunter@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexa", "Hunters", "alexa@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexas", "Hortensia", "Hortensia@gmail.com"));

    StringBuilder expectedOutput = new StringBuilder();
    expectedOutput.append("Enter an id and points or 'back' to return\n\n");
    expectedOutput.append("Points updated.\n");

    Method addStudents = MainUi.class.getDeclaredMethod("addPoints");
    addStudents.setAccessible(true);
    addStudents.invoke(mainUi);

    Assertions.assertEquals(expectedOutput.toString(), mainUi.getConsoleOutput().toString());
  }

  @ParameterizedTest
  @CsvSource({
      "'2sd', 'Incorrect id format\n'",
      "'asdasd', 'Incorrect id format\n'",
      "'', 'Incorrect id format\n'",
      "' ', 'Incorrect id format\n'",
      "'10', 'No student is found for id=10\n'",
      "'15', 'No student is found for id=15\n'",
      "'3', 'No student is found for id=3\n'"
  })
  public void shouldPrintDifferentMessagesOfFindStudentMenu(String input, String message)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    provideInput(input + "\nback\n");
    StringBuilder expectedOutput = new StringBuilder();
    expectedOutput.append("Enter an id or 'back' to return\n\n");
    expectedOutput.append(message);

    Method addStudents = MainUi.class.getDeclaredMethod("findStudent");
    addStudents.setAccessible(true);
    addStudents.invoke(mainUi);

    Assertions.assertEquals(expectedOutput.toString(), mainUi.getConsoleOutput().toString());
  }

  @ParameterizedTest
  @CsvSource({
      "1, '1 2 3 4', '1 points: Java=1; Dsa=2; Databases=3; Spring=4\n'",
      "2, '11 12 13 14', '2 points: Java=11; Dsa=12; Databases=13; Spring=14\n'",
      "3, '40 60 80 100', '3 points: Java=40; Dsa=60; Databases=80; Spring=100\n'"
  })
  public void shouldShowFoundStudentPoints(int studentId, String points, String pointOutput)
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {

    provideInput(studentId + "\nback\n");
    TempStudentStorage.addStudent(new UnsavedStudent("Alex", "Hunter", "hunter@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexa", "Hunters", "alexa@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexas", "Hortensia", "Hortensia@gmail.com"));

    int[] pointsArray = Arrays.stream(points.split("\\s+")).mapToInt(Integer::parseInt).toArray();

    List<AcademicSubject> subjects = TempStudentStorage.getStudentById(studentId).get()
        .getAcademicSubjects();

    for (int i = 0; i < subjects.size(); i++) {
      subjects.get(i).addPoint(pointsArray[i]);
    }

    StringBuilder expectedOutput = new StringBuilder();
    expectedOutput.append("Enter an id or 'back' to return\n\n");
    expectedOutput.append(pointOutput);

    Method addStudents = MainUi.class.getDeclaredMethod("findStudent");
    addStudents.setAccessible(true);
    addStudents.invoke(mainUi);

    Assertions.assertEquals(expectedOutput.toString(), mainUi.getConsoleOutput().toString());
  }

  //Порядок предметов по id - Java, Dsa, Databases, Spring
  public static Stream<Arguments> provideArgumentsForRankedListGetter() {
    return Stream.of(
        Arguments.of(new int[]{4, 3, 2, 1}, new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0},
            new String[]{"Java", "DSA", "Databases", "Spring"},
            (Predicate<AcademicSubject>) (academicSubject -> academicSubject.getPoint() > 0),
            Collectors.counting()),
        Arguments.of(new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1},
            new String[]{"DSA", "Databases", "Java", "Spring"},
            (Predicate<AcademicSubject>) (academicSubject -> academicSubject.getPoint() > 0),
            Collectors.counting()),
        Arguments.of(new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0},
            new String[]{"Java"},
            (Predicate<AcademicSubject>) (academicSubject -> academicSubject.getPoint() > 0),
            Collectors.counting()),
        Arguments.of(new int[]{0, 1, 0, 0}, new int[]{0, 1, 0, 0}, new int[]{0, 1, 0, 0},
            new String[]{"DSA"},
            (Predicate<AcademicSubject>) (academicSubject -> academicSubject.getPoint() > 0),
            Collectors.counting()),
        Arguments.of(new int[]{0, 0, 1, 0}, new int[]{0, 0, 1, 0}, new int[]{0, 0, 1, 0},
            new String[]{"Databases"},
            (Predicate<AcademicSubject>) (academicSubject -> academicSubject.getPoint() > 0),
            Collectors.counting()),
        Arguments.of(new int[]{0, 0, 0, 1}, new int[]{0, 0, 0, 1}, new int[]{0, 0, 0, 1},
            new String[]{"Spring"},
            (Predicate<AcademicSubject>) (academicSubject -> academicSubject.getPoint() > 0),
            Collectors.counting()),
        Arguments.of(new int[]{1, 1, 0, 4}, new int[]{3, 0, 0, 4}, new int[]{0, 0, 0, 4},
            new String[]{"Spring", "Java", "DSA"},
            (Predicate<AcademicSubject>) (academicSubject -> academicSubject.getPoint() > 0),
            Collectors.counting()),
        Arguments.of(new int[]{1, 0, 0, 4}, new int[]{1, 0, 0, 4}, new int[]{1, 0, 0, 4},
            new String[]{"Java", "Spring"},
            (Predicate<AcademicSubject>) (academicSubject -> academicSubject.getPoint() > 0),
            Collectors.counting()),
        Arguments.of(new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0},
            new String[]{},
            (Predicate<AcademicSubject>) (academicSubject -> academicSubject.getPoint() > 0),
            Collectors.counting()),

        Arguments.of(new int[]{4, 3, 2, 1}, new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0},
            new String[]{"Java", "DSA", "Databases", "Spring"},
            (Predicate<AcademicSubject>) (academicSubject -> true),
            Collectors.summingInt(AcademicSubject::getPoint)),
        Arguments.of(new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1},
            new String[]{"DSA", "Databases", "Java", "Spring"},
            (Predicate<AcademicSubject>) (academicSubject -> true),
            Collectors.summingInt(AcademicSubject::getPoint)),
        Arguments.of(new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0},
            new String[]{"Java", "DSA", "Databases", "Spring"},
            (Predicate<AcademicSubject>) (academicSubject -> true),
            Collectors.summingInt(AcademicSubject::getPoint)),
        Arguments.of(new int[]{0, 1, 0, 0}, new int[]{0, 1, 0, 0}, new int[]{0, 1, 0, 0},
            new String[]{"DSA", "Databases", "Java", "Spring"},
            (Predicate<AcademicSubject>) (academicSubject -> true),
            Collectors.summingInt(AcademicSubject::getPoint)),
        Arguments.of(new int[]{0, 0, 1, 0}, new int[]{0, 0, 1, 0}, new int[]{0, 0, 1, 0},
            new String[]{"Databases", "DSA", "Java", "Spring"},
            (Predicate<AcademicSubject>) (academicSubject -> true),
            Collectors.summingInt(AcademicSubject::getPoint)),
        Arguments.of(new int[]{0, 0, 0, 1}, new int[]{0, 0, 0, 1}, new int[]{0, 0, 0, 1},
            new String[]{"Spring", "DSA", "Databases", "Java"},
            (Predicate<AcademicSubject>) (academicSubject -> true),
            Collectors.summingInt(AcademicSubject::getPoint)),
        Arguments.of(new int[]{1, 1, 0, 4}, new int[]{3, 0, 0, 4}, new int[]{0, 0, 0, 4},
            new String[]{"Spring", "Java", "DSA", "Databases"},
            (Predicate<AcademicSubject>) (academicSubject -> true),
            Collectors.summingInt(AcademicSubject::getPoint)),
        Arguments.of(new int[]{1, 0, 0, 4}, new int[]{1, 0, 0, 4}, new int[]{1, 0, 0, 4},
            new String[]{"Spring", "Java", "DSA", "Databases"},
            (Predicate<AcademicSubject>) (academicSubject -> true),
            Collectors.summingInt(AcademicSubject::getPoint)),
        Arguments.of(new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0},
            new String[]{"DSA", "Databases", "Java", "Spring"},
            (Predicate<AcademicSubject>) (academicSubject -> true),
            Collectors.summingInt(AcademicSubject::getPoint))
    );
  }

  @ParameterizedTest
  @MethodSource("provideArgumentsForRankedListGetter")
  public void shouldReturnRankedSubjectList(int[] points1, int[] points2, int[] points3,
      String[] expectedList, Predicate<AcademicSubject> subjectPredicate,
      Collector<AcademicSubject, ?, Long> groupingSign)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    TempStudentStorage.addStudent(new UnsavedStudent("Alex", "Hunter", "hunter@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexis", "Hunters", "hunters@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexa", "Hortensia", "Hortensia@gmail.com"));

    final int[] subjectsId = new int[]{1, 2, 3, 4};
    TempStudentStorage.getStudentById(1).get().updatePoints(subjectsId, points1);
    TempStudentStorage.getStudentById(2).get().updatePoints(subjectsId, points2);
    TempStudentStorage.getStudentById(3).get().updatePoints(subjectsId, points3);

    Method getRankedSubjectList = MainUi.class.getDeclaredMethod(
        "getRankedSubjectList", List.class, Predicate.class, Collector.class);
    getRankedSubjectList.setAccessible(true);
    List<String> rankedList = ((List<Map.Entry<String, Long>>) getRankedSubjectList.invoke(mainUi,
        TempStudentStorage.getStudents(), subjectPredicate, groupingSign)).stream()
        .map(entry -> entry.getKey()).collect(Collectors.toList());

    Assertions.assertArrayEquals(expectedList, rankedList.toArray());
  }

  public static Stream<Arguments> provideArgumentsMostPopularSubject() {
    return Stream.of(
        Arguments.of(new int[]{4, 3, 2, 1}, new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0},
            "Java"),
        Arguments.of(new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1},
            "DSA, Databases, Java, Spring"),
        Arguments.of(new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0},
            "Java"),
        Arguments.of(new int[]{0, 1, 0, 0}, new int[]{0, 1, 0, 0}, new int[]{0, 1, 0, 0},
            "DSA"),
        Arguments.of(new int[]{0, 0, 1, 0}, new int[]{0, 0, 1, 0}, new int[]{0, 0, 1, 0},
            "Databases"),
        Arguments.of(new int[]{0, 0, 0, 1}, new int[]{0, 0, 0, 1}, new int[]{0, 0, 0, 1},
            "Spring"),
        Arguments.of(new int[]{1, 1, 0, 4}, new int[]{3, 0, 0, 4}, new int[]{0, 0, 0, 4},
            "Spring"),
        Arguments.of(new int[]{1, 0, 0, 4}, new int[]{1, 0, 0, 4}, new int[]{1, 0, 0, 4},
            "Java, Spring"),
        Arguments.of(new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0},
            "n/a")
    );
  }

  @ParameterizedTest
  @MethodSource("provideArgumentsMostPopularSubject")
  public void shouldReturnMostPopularSubjects(int[] points1, int[] points2, int[] points3,
      String expectedMostPopularSubjects)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    TempStudentStorage.addStudent(new UnsavedStudent("Alex", "Hunter", "hunter@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexis", "Hunters", "hunters@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexa", "Hortensia", "Hortensia@gmail.com"));

    final int[] subjectsId = new int[]{1, 2, 3, 4};
    TempStudentStorage.getStudentById(1).get().updatePoints(subjectsId, points1);
    TempStudentStorage.getStudentById(2).get().updatePoints(subjectsId, points2);
    TempStudentStorage.getStudentById(3).get().updatePoints(subjectsId, points3);

    Method getMostPopularSubject = MainUi.class.getDeclaredMethod("getMostPopularSubject");
    getMostPopularSubject.setAccessible(true);
    String mostPopularSubjects = (String) getMostPopularSubject.invoke(mainUi);

    Assertions.assertEquals(expectedMostPopularSubjects, mostPopularSubjects);
  }

  public static Stream<Arguments> provideArgumentsLeastPopularSubject() {
    return Stream.of(
        Arguments.of(new int[]{4, 3, 2, 1}, new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0},
            "DSA, Databases, Spring"),
        Arguments.of(new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1},
            "n/a"),
        Arguments.of(new int[]{0, 1, 0, 0}, new int[]{0, 1, 0, 0}, new int[]{0, 1, 0, 0},
            "Java, Databases, Spring"),
        Arguments.of(new int[]{0, 0, 1, 0}, new int[]{0, 0, 1, 0}, new int[]{0, 0, 1, 0},
            "Java, DSA, Spring"),
        Arguments.of(new int[]{0, 0, 0, 1}, new int[]{0, 0, 0, 1}, new int[]{0, 0, 0, 1},
            "Java, DSA, Databases"),
        Arguments.of(new int[]{1, 1, 0, 4}, new int[]{3, 0, 0, 4}, new int[]{0, 0, 0, 4},
            "Databases"),
        Arguments.of(new int[]{1, 0, 0, 4}, new int[]{1, 0, 0, 4}, new int[]{1, 0, 0, 4},
            "DSA, Databases"),
        Arguments.of(new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0},
            "n/a")
    );
  }

  @ParameterizedTest
  @MethodSource("provideArgumentsLeastPopularSubject")
  public void shouldReturnLeastPopularSubjects(int[] points1, int[] points2, int[] points3,
      String expectedLeastPopularSubjects)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    TempStudentStorage.addStudent(new UnsavedStudent("Alex", "Hunter", "hunter@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexis", "Hunters", "hunters@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexa", "Hortensia", "Hortensia@gmail.com"));

    final int[] subjectsId = new int[]{1, 2, 3, 4};
    TempStudentStorage.getStudentById(1).get().updatePoints(subjectsId, points1);
    TempStudentStorage.getStudentById(2).get().updatePoints(subjectsId, points2);
    TempStudentStorage.getStudentById(3).get().updatePoints(subjectsId, points3);

    Method getLeastPopularSubject = MainUi.class.getDeclaredMethod("getLeastPopularSubject");
    getLeastPopularSubject.setAccessible(true);
    String leastPopularSubjects = (String) getLeastPopularSubject.invoke(mainUi);

    Assertions.assertEquals(expectedLeastPopularSubjects, leastPopularSubjects);
  }

  public static Stream<Arguments> provideArgumentsForRankedActivitySubject() {
    return Stream.of(
        Arguments.of(new int[]{4, 3, 2, 1}, new int[]{4, 3, 2, 1}, new int[]{4, 3, 2, 1},
            "Spring", Activity.LOWEST),
        Arguments.of(new int[]{1, 2, 6, 5}, new int[]{1, 2, 1, 1}, new int[]{1, 2, 6, 5},
            "Java", Activity.LOWEST),
        Arguments.of(new int[]{4, 2, 6, 5}, new int[]{4, 2, 6, 7}, new int[]{4, 2, 6, 5},
            "DSA", Activity.LOWEST),
        Arguments.of(new int[]{1, 1, 0, 4}, new int[]{3, 0, 0, 4}, new int[]{0, 0, 0, 4},
            "Databases", Activity.LOWEST),
        Arguments.of(new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1},
            "Spring", Activity.LOWEST),
        Arguments.of(new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0},
            "n/a", Activity.LOWEST),
        Arguments.of(new int[]{4, 3, 2, 1}, new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0},
            "Java", Activity.HIGHEST),
        Arguments.of(new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1},
            "DSA", Activity.HIGHEST),
        Arguments.of(new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0},
            "Java", Activity.HIGHEST),
        Arguments.of(new int[]{0, 1, 0, 0}, new int[]{0, 1, 0, 0}, new int[]{0, 1, 0, 0},
            "DSA", Activity.HIGHEST),
        Arguments.of(new int[]{0, 0, 1, 0}, new int[]{0, 0, 1, 0}, new int[]{0, 0, 1, 0},
            "Databases", Activity.HIGHEST),
        Arguments.of(new int[]{0, 0, 0, 1}, new int[]{0, 0, 0, 1}, new int[]{0, 0, 0, 1},
            "Spring", Activity.HIGHEST),
        Arguments.of(new int[]{1, 1, 0, 4}, new int[]{3, 0, 0, 4}, new int[]{0, 0, 0, 4},
            "Spring", Activity.HIGHEST),
        Arguments.of(new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0},
            "n/a", Activity.HIGHEST)
    );
  }

  @ParameterizedTest
  @MethodSource("provideArgumentsForRankedActivitySubject")
  public void shouldReturnLowestActivitySubjects(int[] points1, int[] points2, int[] points3,
      String expectedMostPopularSubjects, Activity activity)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    TempStudentStorage.addStudent(new UnsavedStudent("Alex", "Hunter", "hunter@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexis", "Hunters", "hunters@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexa", "Hortensia", "Hortensia@gmail.com"));

    final int[] subjectsId = new int[]{1, 2, 3, 4};
    TempStudentStorage.getStudentById(1).get().updatePoints(subjectsId, points1);
    TempStudentStorage.getStudentById(2).get().updatePoints(subjectsId, points2);
    TempStudentStorage.getStudentById(3).get().updatePoints(subjectsId, points3);

    Method getRankedActivitySubject = MainUi.class.getDeclaredMethod("getRankedActivitySubject",
        Activity.class);
    getRankedActivitySubject.setAccessible(true);
    String mostPopularSubjects = (String) getRankedActivitySubject.invoke(mainUi, activity);

    Assertions.assertEquals(expectedMostPopularSubjects, mostPopularSubjects);
  }

  public static Stream<Arguments> provideArgumentsForRankedDifficultSubject() {
    return Stream.of(
        Arguments.of(new int[]{4, 3, 2, 1}, new int[]{4, 3, 2, 1}, new int[]{4, 3, 2, 1},
            "Spring", Difficult.HARD),
        Arguments.of(new int[]{1, 2, 6, 5}, new int[]{1, 2, 1, 1}, new int[]{1, 2, 6, 5},
            "Java", Difficult.HARD),
        Arguments.of(new int[]{4, 2, 6, 5}, new int[]{4, 2, 6, 7}, new int[]{4, 2, 6, 5},
            "DSA", Difficult.HARD),
        Arguments.of(new int[]{1, 1, 0, 4}, new int[]{3, 1, 0, 4}, new int[]{0, 0, 0, 4},
            "Databases", Difficult.HARD),
        Arguments.of(new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1},
            "Spring", Difficult.HARD),
        Arguments.of(new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0},
            "n/a", Difficult.HARD),
        Arguments.of(new int[]{4, 3, 2, 1}, new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0},
            "Java", Difficult.EASY),
        Arguments.of(new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1},
            "DSA", Difficult.EASY),
        Arguments.of(new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 0},
            "Java", Difficult.EASY),
        Arguments.of(new int[]{0, 1, 0, 0}, new int[]{0, 1, 0, 0}, new int[]{0, 1, 0, 0},
            "DSA", Difficult.EASY),
        Arguments.of(new int[]{0, 0, 1, 0}, new int[]{0, 0, 1, 0}, new int[]{0, 0, 1, 0},
            "Databases", Difficult.EASY),
        Arguments.of(new int[]{0, 0, 0, 1}, new int[]{0, 0, 0, 1}, new int[]{0, 0, 0, 1},
            "Spring", Difficult.EASY),
        Arguments.of(new int[]{1, 1, 0, 4}, new int[]{3, 0, 0, 4}, new int[]{0, 0, 0, 4},
            "Spring", Difficult.EASY),
        Arguments.of(new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0},
            "n/a", Difficult.EASY)
    );
  }

  @ParameterizedTest
  @MethodSource("provideArgumentsForRankedDifficultSubject")
  public void shouldReturnActivitySubjects(int[] points1, int[] points2, int[] points3,
      String expectedMostPopularSubjects, Difficult difficult)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    TempStudentStorage.addStudent(new UnsavedStudent("Alex", "Hunter", "hunter@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexis", "Hunters", "hunters@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexa", "Hortensia", "Hortensia@gmail.com"));

    final int[] subjectsId = new int[]{1, 2, 3, 4};
    TempStudentStorage.getStudentById(1).get().updatePoints(subjectsId, points1);
    TempStudentStorage.getStudentById(2).get().updatePoints(subjectsId, points2);
    TempStudentStorage.getStudentById(3).get().updatePoints(subjectsId, points3);

    Method getRankedActivitySubject = MainUi.class.getDeclaredMethod(
        "getRankedDifficultSubject", Difficult.class);
    getRankedActivitySubject.setAccessible(true);
    String mostPopularSubjects = (String) getRankedActivitySubject.invoke(mainUi, difficult);

    Assertions.assertEquals(expectedMostPopularSubjects, mostPopularSubjects);
  }


  public static Stream<Arguments> provideStatsForPrintStatistics() {
    return Stream.of(
        Arguments.of(new String[]{"Java", "DSA", "Java", "DSA", "Databases", "Spring"},
            "Most popular: Java\nLeast  popular: DSA\nHighest activity: Java\nLowest activity: DSA\nEasiest course: Databases\nHardest course: Spring"),
        Arguments.of(new String[]{"n/a", "n/a", "n/a", "n/a", "n/a", "n/a"},
            "Most popular: n/a\nLeast  popular: n/a\nHighest activity: n/a\nLowest activity: n/a\nEasiest course: n/a\nHardest course: n/a"
        )
    );
  }

  @ParameterizedTest
  @MethodSource("provideStatsForPrintStatistics")
  public void showSubjectsStatistics(String[] stats, String expectedOutput)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method showSubjectsStatistics = MainUi.class.getDeclaredMethod("showSubjectsStatistics",
        String.class,
        String.class, String.class, String.class, String.class, String.class);
    showSubjectsStatistics.setAccessible(true);

    String output = (String) showSubjectsStatistics.invoke(mainUi, stats[0], stats[1], stats[2],
        stats[3],
        stats[4], stats[5]);
    Assertions.assertEquals(expectedOutput, output);
  }


  public static Stream<Arguments> provideArgumentsForShowSubjectsStatistics() {
    return Stream.of(
        Arguments.of(new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0},
            "Most popular: n/a\nLeast  popular: n/a\nHighest activity: n/a\nLowest activity: n/a\nEasiest course: n/a\nHardest course: n/a\n")
    );
  }

  //доделать тест
  @ParameterizedTest
  @MethodSource("provideArgumentsForShowSubjectsStatistics")
  public void shouldReturnCorrectStatistics(int[] points1, int[] points2, int[] points3,
      String expectedOutput)
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    TempStudentStorage.addStudent(new UnsavedStudent("Alex", "Hunter", "hunter@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexis", "Hunters", "hunters@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexa", "Hortensia", "Hortensia@gmail.com"));

    final int[] subjectsId = new int[]{1, 2, 3, 4};
    TempStudentStorage.getStudentById(1).get().updatePoints(subjectsId, points1);
    TempStudentStorage.getStudentById(2).get().updatePoints(subjectsId, points2);
    TempStudentStorage.getStudentById(3).get().updatePoints(subjectsId, points3);

    Method printStatistics = MainUi.class.getDeclaredMethod("printStatistics");
    printStatistics.setAccessible(true);
    printStatistics.invoke(mainUi);

    Assertions.assertEquals(expectedOutput, mainUi.getConsoleOutput().toString());
  }

  public static Stream<Arguments> provideArgumentsForFormattedStatisticsBySubject() {
    return Stream.of(
        Arguments.of(new int[]{40, 30, 20, 10}, new int[]{10, 40, 30, 20},
            new int[]{10, 20, 40, 30}, "Spring", """
                Spring
                id    points    completed
                3     30        5.5%
                2     20        3.6%
                1     10        1.8%"""),
        Arguments.of(new int[]{40, 30, 20, 10}, new int[]{10, 40, 30, 20},
            new int[]{10, 20, 40, 30}, "Java", """
                Java
                id    points    completed
                1     40        6.7%
                2     10        1.7%
                3     10        1.7%"""),
        Arguments.of(new int[]{40, 30, 20, 10}, new int[]{10, 40, 30, 20},
            new int[]{10, 20, 40, 30}, "Databases", """
                Databases
                id    points    completed
                3     40        8.3%
                2     30        6.2%
                1     20        4.2%"""),
        Arguments.of(new int[]{40, 30, 20, 10}, new int[]{10, 40, 30, 20},
            new int[]{10, 20, 40, 30}, "DSA", """
                DSA
                id    points    completed
                2     40        10.0%
                1     30        7.5%
                3     20        5.0%""")
    );
  }

  @ParameterizedTest
  @MethodSource("provideArgumentsForFormattedStatisticsBySubject")
  public void asdasd(int[] points1, int[] points2, int[] points3, String subject,
      String expectedOutput)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    TempStudentStorage.addStudent(new UnsavedStudent("Alex", "Hunter", "hunter@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexis", "Hunters", "hunters@gmail.com"));
    TempStudentStorage.addStudent(new UnsavedStudent("Alexa", "Hortensia", "Hortensia@gmail.com"));
    final int[] subjectsId = new int[]{1, 2, 3, 4};
    TempStudentStorage.getStudentById(1).get().updatePoints(subjectsId, points1);
    TempStudentStorage.getStudentById(2).get().updatePoints(subjectsId, points2);
    TempStudentStorage.getStudentById(3).get().updatePoints(subjectsId, points3);

    Method showSubjectStatistics = MainUi.class.getDeclaredMethod("showSubjectStatistics",
        String.class);
    showSubjectStatistics.setAccessible(true);
    String s = (String) showSubjectStatistics.invoke(mainUi, subject);

    Assertions.assertEquals(expectedOutput, s);
  }
}
