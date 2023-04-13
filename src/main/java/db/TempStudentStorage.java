package db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import entities.Student;

public class TempStudentStorage {

  private static List<Student> students = new ArrayList<>();
  private static AtomicInteger studentsId = new AtomicInteger(1);

  public static void addStudent(Student student) {
    student.setId(studentsId.getAndIncrement());
    students.add(student);
  }

  public static List<Student> getStudents() {
    return Collections.unmodifiableList(students);
  }

  public static void setStudents(List<Student> newStudents) {
    students = newStudents;
  }

  public static Optional<Student> getStudentById(int id) {
    return students.stream().filter(student -> student.getId() == id).findFirst();
  }

}
