package tracker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.input.UserInputService;
import tracker.session.Session;

public class SessionTest {

  private Session session;

  @BeforeEach
  public void init() {
    session = new Session();
  }

  @Test
  public void shouldPrintProgramTitle()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method printTitle = Session.class.getDeclaredMethod("printTitle");
    printTitle.setAccessible(true);
    String output = (String) printTitle.invoke(session);

    Assertions.assertEquals("Learning Progress Tracker\n", output);
  }

  @Test
  public void newSessionShouldNotBeClosed() {
    Assertions.assertFalse(session.isClosed());
  }

  @Test
  public void startSessionShouldOpenUi() {
    InputStream systemIn = System.in;
    PrintStream systemOut = System.out;
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    System.setIn(new ByteArrayInputStream("exit\r\n".getBytes()));
    System.setOut(new PrintStream(output));

    session.getUi().setInputReader(new UserInputService());
    session.start();

    System.setIn(systemIn);
    System.setOut(systemOut);

    String expectedOutput = "Learning Progress Tracker\nBye!";
    Assertions.assertEquals(expectedOutput, output.toString());
  }
}
