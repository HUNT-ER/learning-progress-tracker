import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import enums.Course;

public class EnumTest {

  @ParameterizedTest
  @CsvSource({
      "'java', true",
      "'dsa', true",
      "'databases', true",
      "'spring', true",
      "'', false",
      "'sdadsa', false",
      "'-', false",
      "'1', false"
  })
  public void shouldReturnTrueIfCourseIsExists(String input, boolean isCorrect) {
    Assertions.assertEquals(isCorrect, Course.isExistedCourse(input));
  }
}
