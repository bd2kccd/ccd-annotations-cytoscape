package edu.pitt.cs.admt.cytoscape.annotations.db.entity;

/**
 * @author Nikos R. Katsipoulakis
 */
public class Edge {
  
  private int suid;
  
  private int source;
  
  private int destination;
  
  public Edge(int suid, int source, int destination) {
    if (suid < 0 || source < 0 || destination < 0)
      throw new IllegalArgumentException("invalid arguments given");
    this.suid = suid;
    this.source = source;
    this.destination = destination;
  }
  
  public int getSuid() {
    return suid;
  }
  
  public void setSuid(int suid) {
    if (suid < 0)
      throw new IllegalArgumentException("negative id given");
    this.suid = suid;
  }
  
  public int getSource() {
    return source;
  }
  
  public void setSource(int source) {
    if (source < 0)
      throw new IllegalArgumentException("negative source given");
    this.source = source;
  }
  
  public int getDestination() {
    return destination;
  }
  
  public void setDestination(int destination) {
    if (destination < 0)
      throw new IllegalArgumentException("negative destination given");
    this.destination = destination;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    
    Edge edge = (Edge) o;
    
    if (suid != edge.suid) return false;
    if (source != edge.source) return false;
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
