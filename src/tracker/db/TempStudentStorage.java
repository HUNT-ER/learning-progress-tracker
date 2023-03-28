package tracker.db;

import java.util.ArrayList;
import java.util.List;
import tracker.entities.Student;

public class TempStudentStorage {

  private static List<Student> students = new ArrayList<>();

  public static void addStudent(Student student) {
    students.add(student);
  }

  public static List<Student> getStudents() {
    return students;
  }

  public static void setStudents(List<Student> newStudents) {
    students = newStudents;
  }

}
