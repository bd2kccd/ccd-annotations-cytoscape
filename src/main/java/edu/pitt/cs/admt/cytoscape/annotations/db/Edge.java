package edu.pitt.cs.admt.cytoscape.annotations.db;

/**
 * @author Nikos R. Katsipoulakis
 */
public class Edge {
  
  private int suid;
  
  private int source;
  
  private int destination;
  
  public Edge(int suid, int source, int destination) {
    this.suid = suid;
    this.source = source;
    this.destination = destination;
  }
  
  public int getSuid() {
    return suid;
  }
  
  public void setSuid(int suid) {
    this.suid = suid;
  }
  
  public int getSource() {
    return source;
  }
  
  public void setSource(int source) {
    this.source = source;
  }
  
  public int getDestination() {
    return destination;
  }
  
  public void setDestination(int destination) {
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
