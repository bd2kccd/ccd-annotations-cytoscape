package edu.pitt.cs.admt.cytoscape.annotations.db.entity;

import com.google.common.base.Preconditions;

/**
 * @author Nikos R. Katsipoulakis
 */
public class Edge {
  
  private int suid;
  
  private int source;
  
  private int destination;
  
  public Edge(int suid, int source, int destination) {
    Preconditions.checkArgument(suid >= 0 && source >= 0 && destination >= 0);
    this.suid = suid;
    this.source = source;
    this.destination = destination;
  }
  
  public int getSuid() {
    return suid;
  }
  
  public void setSuid(int suid) {
    Preconditions.checkArgument(suid >= 0);
    this.suid = suid;
  }
  
  public int getSource() {
    return source;
  }
  
  public void setSource(int source) {
    Preconditions.checkArgument(source >= 0);
    this.source = source;
  }
  
  public int getDestination() {
    return destination;
  }
  
  public void setDestination(int destination) {
    Preconditions.checkArgument(destination >= 0);
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
