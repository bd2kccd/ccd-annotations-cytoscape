package edu.pitt.cs.admt.cytoscape.annotations.db.entity;

import com.sun.istack.internal.NotNull;

/**
 * @author Nikos R. Katsipoulakis
 */
public enum ExtendedAttributeType {
  CHAR("CHAR"),
  BOOLEAN("BOOLEAN"),
  INT("INT"),
  FLOAT("FLOAT"),
  STRING("STRING");
  
  private final String name;
  
  private ExtendedAttributeType(String s) {
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

  public static ExtendedAttributeType parse(@NotNull final String s) {
    switch (s) {
      case "BOOLEAN":
        return ExtendedAttributeType.BOOLEAN;
      case "INT":
        return ExtendedAttributeType.INT;
      case "FLOAT":
        return ExtendedAttributeType.FLOAT;
      case "CHAR":
        return ExtendedAttributeType.CHAR;
      case "STRING":
        return ExtendedAttributeType.STRING;
      default:
        throw new IllegalArgumentException("invalid literal given: " + s);
    }
  }
}
