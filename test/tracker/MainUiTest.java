package tracker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.db.TempStudentStorage;
import tracker.entities.Student;
import tracker.entities.UnsavedStudent;
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
  public void restoreStudentsStorage() {
    TempStudentStorage.setStudents(new ArrayList<>());
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

  @Test
  public void addedStudentShouldBeEqual()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method registerStudent = MainUi.class.getDeclaredMethod("registerStudent", String.class);
    registerStudent.setAccessible(true);

    String testStudent = "Alex Hunter hunter@gmail.com";
    //добавляем студента в хранилище
    registerStudent.invoke(mainUi, testStudent);

    List<Student> students = TempStudentStorage.getStudents();
    Student expectedStudent = new UnsavedStudent("Alex", "Hunter", "hunter@gmail.com");
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

}
