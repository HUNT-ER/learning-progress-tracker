import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import input.UserInputService;

public class UserInputServiceTest {

  private static final InputStream SYSTEM_IN = System.in;

  @AfterEach
  public void restoreSystemInputStream() {
    System.setIn(SYSTEM_IN);
  }

  private void provideInput(String input) {
    ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
    System.setIn(in);
  }

  @Test
  public void returnedStringShouldBeVoid() {
    provideInput("");
    UserInputService inputReader = new UserInputService();
    Assertions.assertEquals("", inputReader.getStringInput());
  }

  @Test
  public void returnedStringShouldBeEqualToInput() {
    provideInput("First input\r\n");
    UserInputService inputReader = new UserInputService();
    Assertions.assertEquals("First input", inputReader.getStringInput());
  }

}
