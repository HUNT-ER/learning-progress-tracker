package tracker.entities.subjects;

public abstract class AcademicSubject {

  protected int id;
  protected int point;
  protected String name;
  protected int maxPointsValue;

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

}
