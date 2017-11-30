package edu.pitt.cs.admt.cytoscape.annotations.db.entity;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.UUID;

/**
 * @author Nikos R. Katsipoulakis
 */
public class AnnotToEntity {
  
  private UUID annotationId;
  
  private int entityId;
  
  private Object value;
  
  public AnnotToEntity(UUID annotationId, int entityId, Object value) {
    Preconditions.checkArgument(entityId >= 0);
    if (value != null)
      Preconditions.checkArgument(value instanceof Character || value instanceof Boolean ||
      value instanceof Integer || value instanceof Float || value instanceof String);
    this.annotationId = annotationId;
    this.entityId = entityId;
    this.value = value;
  }
  
  public UUID getAnnotationId() {
    return annotationId;
  }
  
  public void setAnnotationId(UUID annotationId) {
    this.annotationId = annotationId;
  }
  
  public int getEntityId() {
    return entityId;
  }
  
  public void setEntityId(int entityId) {
    Preconditions.checkArgument(entityId >= 0);
    this.entityId = entityId;
  }
  
  public Object getValue() {
    return value;
  }
  
  public void setValue(Object value) {
    if (value != null)
      Preconditions.checkArgument(value instanceof Character || value instanceof Boolean ||
          value instanceof Integer || value instanceof Float || value instanceof String);
    this.value = value;
  }
}
