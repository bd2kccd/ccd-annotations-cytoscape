package edu.pitt.cs.admt.cytoscape.annotations.db.entity;

/**
 * Class representing an Edge
 * @author Nikos R. Katsipoulakis
 */
public class Edge {

  private int suid;

  private int source;

  private int destination;
  
  /**
   *
   * @param suid the unique suid for the edge
   * @param source the source {@link Node}'s <code>suid</code>
   * @param destination the destination {@link Node}'s <code>suid</code>
   * @throws IllegalArgumentException if <code>suid</code>, <code>source</code>, or
   * <code>destination</code> values are negative
   */
  public Edge(int suid, int source, int destination) {
    if (suid < 0 || source < 0 || destination < 0) {
      throw new IllegalArgumentException("invalid arguments given");
    }
    this.suid = suid;
    this.source = source;
    this.destination = destination;
  }
  
  /**
   *
   * @return an {@link Edge}'s id.
   */
  public int getSuid() {
    return suid;
  }
  
  /**
   *
   * @param suid
   */
  public void setSuid(int suid) {
    if (suid < 0) {
      throw new IllegalArgumentException("negative id given");
    }
    this.suid = suid;
  }

  public int getSource() {
    return source;
  }

  public void setSource(int source) {
    if (source < 0) {
      throw new IllegalArgumentException("negative source given");
    }
    this.source = source;
  }

  public int getDestination() {
    return destination;
  }

  public void setDestination(int destination) {
    if (destination < 0) {
      throw new IllegalArgumentException("negative destination given");
    }
    this.destination = destination;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Edge edge = (Edge) o;

    if (suid != edge.suid) {
      return false;
    }
    if (source != edge.source) {
      return false;
    }
    return destination == edge.destination;
  }

  @Override
  public int hashCode() {
    int result = suid;
    result = 31 * result + source;
    result = 31 * result + destination;
    return result;
  }
}
