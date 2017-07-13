package edu.pitt.cs.admt.cytoscape.annotations.db;

/**
 * @author Nikos R. Katsipoulakis
 */
public class Node {
  
  private int suid;
  
  public Node(int suid) {
    this.suid = suid;
  }
  
  public int getSuid() {
    return suid;
  }
  
  public void setSuid(int suid) {
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
