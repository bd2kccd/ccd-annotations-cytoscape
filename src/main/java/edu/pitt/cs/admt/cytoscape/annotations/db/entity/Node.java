package edu.pitt.cs.admt.cytoscape.annotations.db.entity;

import com.google.common.base.Preconditions;

/**
 * @author Nikos R. Katsipoulakis
 */
public class Node {

  private int suid;

  public Node(int suid) {
    Preconditions.checkArgument(suid >= 0);
    this.suid = suid;
  }

  public int getSuid() {
    return suid;
  }

  public void setSuid(int suid) {
    Preconditions.checkArgument(suid >= 0);
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
