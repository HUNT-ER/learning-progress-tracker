package tracker.enums;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Course {
  JAVA("Java"), DSA("DSA"), DATABASES("Databases"), SPRING("Spring"), UNKNOWN("Unknown");

  private final String name;

  Course(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static boolean isExistedCourse(String name) {
    return Arrays.stream(Course.values()).anyMatch(x -> x.name.equalsIgnoreCase(name.trim()));
  }

  public static Course getCourseByName(String name) {
    if (!isExistedCourse(name)) {
      throw new EnumConstantNotPresentException(Course.class, name);
    }
    return Arrays.stream(Course.values()).filter(x -> x.name.equalsIgnoreCase(name.trim())).findFirst().get();
  }

  public static String[] nameValues() {
    return Arrays.stream(Course.values()).map(course -> course.getName()).filter(course -> !"Unknown".equals(course)).toArray(String[]::new);
  }
}
