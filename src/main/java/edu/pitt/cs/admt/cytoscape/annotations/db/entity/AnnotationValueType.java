package edu.pitt.cs.admt.cytoscape.annotations.db.entity;

/**
 * @author Nikos R. Katsipoulakis
 */
public enum AnnotationValueType {
  CHAR("CHAR"),
  BOOLEAN("BOOLEAN"),
  INT("INT"),
  FLOAT("FLOAT"),
  STRING("STRING");

  private final String name;

  private AnnotationValueType(String s) {
    name = s;
  }

  public static boolean validate(final String s) {
    if (s == null || s.length() == 0 || s.equals("")) {
      return false;
    }
    switch (s) {
      case "BOOLEAN":
      case "INT":
      case "FLOAT":
      case "CHAR":
      case "STRING":
        return true;
      default:
        return false;
    }
  }

  public static AnnotationValueType parse(final String s) {
    switch (s.toUpperCase()) {
      case "BOOLEAN":
        return AnnotationValueType.BOOLEAN;
      case "INT":
        return AnnotationValueType.INT;
      case "FLOAT":
        return AnnotationValueType.FLOAT;
      case "CHAR":
        return AnnotationValueType.CHAR;
      case "STRING":
        return AnnotationValueType.STRING;
      default:
        throw new IllegalArgumentException("invalid literal given: " + s);
    }
  }

  public boolean equalsName(String otherName) {
    return name.equals(otherName);
  }

  public String toString() {
    return this.name;
  }
}
