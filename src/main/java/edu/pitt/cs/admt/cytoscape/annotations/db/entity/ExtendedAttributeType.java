package edu.pitt.cs.admt.cytoscape.annotations.db.entity;

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
}
