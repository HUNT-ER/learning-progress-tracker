package tracker.entities;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Student extends Entity {

  private String lastname;
  private String email;
  private List<AcademicSubject> academicSubjects;

  public Student(int id, String name, String lastname, String email) {
    super(id, name);
    this.lastname = lastname;
    this.email = email;
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
    academicSubjects.add(new AcademicSubject(1, "Java"));
    academicSubjects.add(new AcademicSubject(2, "DSA"));
    academicSubjects.add(new AcademicSubject(3, "Databases"));
    academicSubjects.add(new AcademicSubject(4, "Spring"));
  }

  public List<AcademicSubject> getAcademicSubjects() {
    return Collections.unmodifiableList(academicSubjects);
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

  private Optional<AcademicSubject> getAcademicSubject(int id) {
    return academicSubjects.stream().filter(subject -> subject.getId() == id).findFirst();
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
    return lastname.equals(student.lastname) && email.equals(student.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), lastname, email);
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
