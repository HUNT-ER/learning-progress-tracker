package tracker;

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
import tracker.entities.AcademicSubject;
import tracker.entities.Entity;
import tracker.entities.Student;

public class StudentTest {

  private Student student;

  @BeforeEach
  public void initStudent() {
    this.student = new Student(1, "Alex", "Hunter", "hunter@gmail.com");
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
    Assertions.assertArrayEquals(new int[] {0,0,0,0}, studentPoints);
  }

  @ParameterizedTest
  @CsvSource({"1, Java", "2, DSA", "3, Databases", "4, Spring"})
  public void newStudentShouldHaveDefaultSubjects(int subjectId, String subject) {
    Assertions.assertEquals(subject, student.getAcademicSubjects().get(subjectId - 1).getName());
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

  @ParameterizedTest
  @MethodSource("provideArgumentsForUpdatePoints")
  public void shouldReturnFalseIfArgsHasDifferentLength(int[] points, int[] subjectsId,
      boolean isUpdated) {
    Assertions.assertEquals(isUpdated, student.updatePoints(subjectsId, points));
  }

  private static Stream<Arguments> provideCorrectArgumentsForUpdatePoints() {
    return Stream.of(
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{1, 2, 3, 4}),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{10, 2, 3, 5}),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{18, 2, 3, 4}),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{3, 2, 26, 4}),
        Arguments.of(new int[]{1, 2, 3, 4}, new int[]{156656, 20214, 302222, 45})
    );
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

}
