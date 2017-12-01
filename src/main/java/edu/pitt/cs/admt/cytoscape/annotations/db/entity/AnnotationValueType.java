package edu.pitt.cs.admt.cytoscape.annotations.db.entity;

import com.sun.istack.internal.NotNull;

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
  
  public boolean equalsName(String otherName) {
    return name.equals(otherName);
  }
  
  public String toString() {
    return this.name;
  }

  public static boolean validate(@NotNull final String s) {
    if (s == null || s.length() == 0 || s.equals(""))
      return false;
    switch(s) {
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

  public static AnnotationValueType parse(@NotNull final String s) {
    switch (s) {
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
}
