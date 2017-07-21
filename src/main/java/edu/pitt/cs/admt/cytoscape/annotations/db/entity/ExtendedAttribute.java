package edu.pitt.cs.admt.cytoscape.annotations.db.entity;

import com.google.common.base.Preconditions;

/**
 * @author Nikos R. Katsipoulakis
 */
public class ExtendedAttribute {
  
  private int id;
  
  private String name;
  
  private ExtendedAttributeType type;
  
  public ExtendedAttribute(int id, String name, ExtendedAttributeType type) {
    Preconditions.checkArgument(id >= 0);
    Preconditions.checkArgument(name != null && name.length() < 32);
    Preconditions.checkArgument(type != null);
    this.id = id;
    this.name = name;
    this.type = type;
  }
  
  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    Preconditions.checkArgument(id >= 0);
    this.id = id;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    Preconditions.checkArgument(name != null && name.length() < 32);
    this.name = name;
  }
  
  public ExtendedAttributeType getType() {
    return type;
  }
  
  public void setType(ExtendedAttributeType type) {
    Preconditions.checkArgument(type != null);
    this.type = type;
  }
}
