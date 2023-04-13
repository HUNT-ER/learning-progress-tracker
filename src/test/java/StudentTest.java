import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import entities.Student;
import entities.subjects.AcademicSubject;

public class StudentTest {

  private Student student;

  @BeforeEach
  public void initStudent() {
    this.student = new Student(1, "Alex", "Hunter", "hunter@gmail.com");
  }

  private static Stream<Arguments> provideCorrectArgumentsForUpdatePoints() {
    return Stream.of(
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{1, 2, 3, 4}),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{10, 2, 3, 5}),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{18, 2, 3, 4}),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{3, 2, 26, 4})
    );
  }

  private static Stream<Arguments> provideLargeArgumentsForUpdatePoints() {
    return Stream.of(
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{10000, 200000, 300000, 4000000}),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{1651651, 216516516, 355515, 5321}),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{1823, 2132, 3451, 43231}),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{601, 401, 481, 551}),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{3445, 21111111, 26222222, 4333333}));
  }

  private static Stream<Arguments> provideArgumentsForUpdatePoints() {
    return Stream.of(
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{1, 2, 3}, false),
        Arguments.of(new int[]{1, 2, 3}, new int[]{1, 2, 3, 4}, false),
        Arguments.of(new int[]{10, 15, 20, 25}, new int[]{1, 2, 3, 4}, true),
        Arguments.of(new int[]{10, 15, 20, 25}, new int[]{1, 2, 3, 5}, true),
        Arguments.of(new int[]{10, 15, 20, 25}, new int[]{1, -1, 0, 5}, true),
        Arguments.of(new int[]{10, 15, 20, 25}, new int[]{10, -1, 0, 5}, false),
        Arguments.of(new int[]{-10, 15, 20, 25}, new int[]{1, 2, 3, 4}, true),
        Arguments.of(new int[]{-10, -15, -20, 25}, new int[]{1, 2, 3, 4}, true),
        Arguments.of(new int[]{-10, -15, -20, 25}, new int[]{1, 2, 3, 6}, false),
        Arguments.of(new int[]{10, -15, -20, -25}, new int[]{1, 2, 3, 6}, true),
        Arguments.of(new int[]{10, -15, -20, -25}, new int[]{5, 2, 3, 6}, false)
    );
  }

  private static Stream<Arguments> provideStudentPointsForEnrollTest() {
    return Stream.of(
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{1, 2, 3, 4}, true),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{1, 2, 3, 0}, true),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{1, 2, 0, 0}, true),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{1, 0, 0, 0}, true),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{0, 1, 0, 0}, true),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{0, 0, 1, 0}, true),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{0, 0, 0, 1}, true),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{0, 0, 0, 0}, false)
    );
  }

  @Test
  public void newStudentShouldHaveFourDefaultSubjects() {
    Assertions.assertEquals(4, student.getAcademicSubjects().size());
  }

  @Test
  public void lastNameShouldBeEqual() {
    Assertions.assertEquals("Hunter", student.getLastname());
  }

  @Test
  public void nameShouldBeEqual() {
    Assertions.assertEquals("Alex", student.getName());
  }

  @Test
  public void emailShouldBeEqual() {
    Assertions.assertEquals("hunter@gmail.com", student.getEmail());
  }

  @Test
  public void idShouldBeEqual() {
    Assertions.assertEquals(1, student.getId());
  }

  @Test
  public void newStudentShouldHaveZeroSubjectPoints() {
    int[] studentPoints = student.getAcademicSubjects()
        .stream()
        .sorted(Comparator.comparingInt(AcademicSubject::getId))
        .mapToInt(value -> value.getPoint())
        .toArray();
    Assertions.assertArrayEquals(new int[]{0, 0, 0, 0}, studentPoints);
  }

  @ParameterizedTest
  @CsvSource({"1, Java", "2, DSA", "3, Databases", "4, Spring"})
  public void newStudentShouldHaveDefaultSubjects(int subjectId, String subject) {
    Assertions.assertEquals(subject, student.getAcademicSubjects().get(subjectId - 1).getName());
  }

  @ParameterizedTest
  @CsvSource({"1, Java", "2, Dsa", "3, Databases", "4, Spring"})
  public void newStudentShouldHaveDefaultSubjectsClasses(int subjectId, String subject) {
    Assertions.assertEquals(subject,
        student.getAcademicSubjects().get(subjectId - 1).getClass().getSimpleName());
  }


  @ParameterizedTest
  @MethodSource("provideArgumentsForUpdatePoints")
  public void shouldReturnFalseIfArgsHasDifferentLength(int[] points, int[] subjectsId,
      boolean isUpdated) {
    Assertions.assertEquals(isUpdated, student.updatePoints(subjectsId, points));
  }


  @ParameterizedTest
  @MethodSource("provideCorrectArgumentsForUpdatePoints")
  public void shouldSetNewPoints(int[] subjectsId, int[] points) {
    student.updatePoints(subjectsId, points);
    int[] studentPoints = student.getAcademicSubjects()
        .stream()
        .sorted(Comparator.comparingInt(AcademicSubject::getId))
        .mapToInt(value -> value.getPoint())
        .toArray();
    Assertions.assertArrayEquals(studentPoints, points);
  }

  @ParameterizedTest
  @MethodSource("provideCorrectArgumentsForUpdatePoints")
  public void shouldUpdatePoints(int[] subjectsId, int[] points) {
    student.updatePoints(subjectsId, points);
    student.updatePoints(subjectsId, points);

    int[] expectedPoints = Arrays.stream(points).map(x -> x + x).toArray();

    int[] studentPoints = student.getAcademicSubjects()
        .stream()
        .sorted(Comparator.comparingInt(AcademicSubject::getId))
        .mapToInt(value -> value.getPoint())
        .toArray();

    Assertions.assertArrayEquals(expectedPoints, studentPoints);
  }

  @ParameterizedTest
  @MethodSource("provideLargeArgumentsForUpdatePoints")
  public void shouldSetMaxPointsValue(int[] subjectsId, int[] points) {
    student.updatePoints(subjectsId, points);
    int[] studentPoints = student.getAcademicSubjects()
        .stream()
        .sorted(Comparator.comparingInt(AcademicSubject::getId))
        .mapToInt(value -> value.getPoint())
        .toArray();
    Assertions.assertArrayEquals(studentPoints, new int[]{600, 400, 480, 550});
  }

  @ParameterizedTest
  @MethodSource("provideStudentPointsForEnrollTest")
  public void shouldReturnCorrectIsEnrolledValue(int[] subjectsId, int[] points,
      boolean isEnrolled) {
    student.updatePoints(subjectsId, points);
    Assertions.assertEquals(isEnrolled, student.isEnrolled());
  }

  public static Stream<Arguments> provideArgumentsForGettingSubjectStats() {
    return Stream.of(
        Arguments.of(new String[]{"Java", "10", "1.7%"}, "java"),
        Arguments.of(new String[]{"DSA", "10", "2.5%"}, "dsa"),
        Arguments.of(new String[]{"Databases", "10", "2.1%"}, "databases"),
        Arguments.of(new String[]{"Spring", "10", "1.8%"}, "spring")
    );
  }

  @ParameterizedTest
  @MethodSource("provideArgumentsForGettingSubjectStats")
  public void shouldReturnArrayOfSubjectStats(String[] stats, String subject) {
    student.updatePoints(new int[]{1, 2, 3, 4}, new int[]{10, 10, 10, 10});

    Assertions.assertArrayEquals(stats, student.getAcademicSubjectStats(subject));
  }

  @Test
  public void test() {
    Assertions.assertEquals(6.2,
        new BigDecimal(0.0625*100).setScale(1, RoundingMode.HALF_DOWN).doubleValue());
  }

}
