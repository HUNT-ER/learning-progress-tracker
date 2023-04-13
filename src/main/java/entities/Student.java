package entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import entities.subjects.AcademicSubject;
import entities.subjects.Databases;
import entities.subjects.Dsa;
import entities.subjects.Java;
import entities.subjects.Spring;
import enums.Course;

public class Student extends Entity {

  private String lastname;
  private String email;
  private List<AcademicSubject> academicSubjects;
  private boolean isNotified;

  public Student(int id, String name, String lastname, String email) {
    super(id, name);
    this.lastname = lastname;
    this.email = email;
    isNotified = true;
    addDefaultAcademicSubjects();
  }

  public String getLastname() {
    return lastname;
  }

  public String getEmail() {
    return email;
  }

  private void addDefaultAcademicSubjects() {
    this.academicSubjects = new LinkedList<>();
    academicSubjects.add(new Java());
    academicSubjects.add(new Dsa());
    academicSubjects.add(new Databases());
    academicSubjects.add(new Spring());
  }

  public List<AcademicSubject> getAcademicSubjects() {
    return Collections.unmodifiableList(academicSubjects);
  }

  private Optional<AcademicSubject> getAcademicSubject(int id) {
    return academicSubjects.stream().filter(subject -> subject.getId() == id).findFirst();
  }

  //если предмет не существует, то выбрасывается исключение NoSuchElementsException
  public AcademicSubject getAcademicSubjectByName(String subject) {
    if (!Course.isExistedCourse(subject)) {
      throw new NoSuchElementException();
    }
    return academicSubjects.stream()
        .filter(academicSubject -> academicSubject.getName().equalsIgnoreCase(subject)).findFirst()
        .get();
  }

  public String[] getAcademicSubjectStats(String subject) {
    AcademicSubject academicSubject = getAcademicSubjectByName(subject);
    return new String[]{academicSubject.getName(), Integer.toString(academicSubject.getPoint()),
        new BigDecimal(
            (double) academicSubject.getPoint() / academicSubject.getMaxPointsValue()).setScale(3,
            RoundingMode.HALF_DOWN).scaleByPowerOfTen(2) + "%"};
  }

  public boolean updatePoints(int[] subjectsId, int[] points) {
    if (points.length != subjectsId.length) {
      return false;
    }
    boolean isUpdated = false;
    for (int i = 0; i < subjectsId.length; i++) {
      Optional<AcademicSubject> subject = getAcademicSubject(subjectsId[i]);
      if (subject.isPresent() && points[i] >= 0) {
        subject.get().addPoint(points[i]);
        isUpdated = true;
      }
    }
    return isUpdated;
  }

  public boolean isEnrolled() {
    return academicSubjects.stream().filter(subject -> subject.getPoint() > 0).count() > 0 ? true
        : false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Student)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Student student = (Student) o;
    return lastname.equals(student.lastname) && email.equals(student.email)
        && academicSubjects.equals(student.academicSubjects);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), lastname, email, academicSubjects);
  }

  @Override
  public String toString() {
    return "Student{" +
        "lastname='" + lastname + '\'' +
        ", email='" + email + '\'' +
        ", id=" + id +
        ", name='" + name + '\'' +
        '}';
  }

}
