package edu.pitt.cs.admt.cytoscape.annotations.db.entity;

/**
 * Used to hold the information of a Node
 * @author Nikos R. Katsipoulakis
 */
public class Node {

  private int suid;
  
  /**
   * Default constructor
   * @param suid the node's id
   * @throws IllegalArgumentException if <code>suid</code> is negative
   */
  public Node(int suid) {
    if (suid < 0) {
      throw new IllegalArgumentException("negative id given.");
    }
    this.suid = suid;
  }
  
  /**
   * Returns the <code>suid</code> of a node
   * @return the node's id
   */
  public int getSuid() {
    return suid;
  }
  
  /**
   * <code>suid</code> mutator
   * @param suid the new id value
   * @throws IllegalArgumentException if <code>suid</code> is negative
   */
  public void setSuid(int suid) {
    if (suid < 0) {
      throw new IllegalArgumentException("negative id given.");
    }
    this.suid = suid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Node node = (Node) o;

    return suid == node.suid;
  }

  @Override
  public int hashCode() {
    return suid;
  }
}
