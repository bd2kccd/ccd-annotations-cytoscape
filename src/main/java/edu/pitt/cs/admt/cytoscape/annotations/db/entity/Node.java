package edu.pitt.cs.admt.cytoscape.annotations.db.entity;

/**
 * @author Nikos R. Katsipoulakis
 */
public class Node {
  
  private int suid;
  
  public Node(int suid) {
    if (suid < 0)
      throw new IllegalArgumentException("negative id given.");
    this.suid = suid;
  }
  
  public int getSuid() {
    return suid;
  }
  
  public void setSuid(int suid) {
    if (suid < 0)
      throw new IllegalArgumentException("negative id given.");
    this.suid = suid;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    
    Node node = (Node) o;
  
    return suid == node.suid;
  }
  
  @Override
  public int hashCode() {
    return suid;
  }
}
