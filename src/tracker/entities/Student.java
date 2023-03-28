package tracker.entities;

import java.util.Objects;

public class Student extends Entity {

  private String lastname;
  private String email;

  public Student(int id, String name, String lastname, String email) {
    super(id, name);
    this.lastname = lastname;
    this.email = email;
  }

  public String getLastname() {
    return lastname;
  }

  public String getEmail() {
    return email;
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
}
