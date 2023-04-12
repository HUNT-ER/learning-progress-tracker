package tracker.entities.subjects;

import java.util.Objects;

public abstract class AcademicSubject {

  protected int id;
  protected int point;
  protected String name;
  protected int maxPointsValue;
  protected boolean isNotified;

  protected AcademicSubject() {
    point = 0;
    isNotified = false;
  }
  public int getPoint() {
    return point;
  }

  public void addPoint(int point) {
    int newPoints = this.point + point;
    if (newPoints > maxPointsValue) {
      this.point = maxPointsValue;
    } else {
      this.point = newPoints;
    }
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getMaxPointsValue() {
    return maxPointsValue;
  }

  public boolean isNotified() {
    return isNotified;
  }

   public void setNotify() {
    isNotified = true;
   }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AcademicSubject)) {
      return false;
    }
    AcademicSubject that = (AcademicSubject) o;
    return id == that.id && point == that.point && maxPointsValue == that.maxPointsValue
        && isNotified == that.isNotified && name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, point, name, maxPointsValue, isNotified);
  }
}
