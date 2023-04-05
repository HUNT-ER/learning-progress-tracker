package tracker.entities;

public class AcademicSubject extends Entity {

  private int point;

  public AcademicSubject(int id, String name) {
    super(id, name);
    point = 0;
  }

  public int getPoint() {
    return point;
  }

  public void addPoint(int point) {
    this.point += point;
  }

}
