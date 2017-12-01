package edu.pitt.cs.admt.cytoscape.annotations.db.entity;

import com.sun.istack.internal.NotNull;
import java.util.UUID;

/**
 * @author Nikos R. Katsipoulakis
 */
public class AnnotToEntity {
  
  private UUID annotationId;
  
  private int entityId;
  
  private Object value;
  
  public AnnotToEntity(@NotNull UUID annotationId, int entityId, Object value) {
    if (entityId < 0) throw new IllegalArgumentException("negative id given.");
    if (value != null) {
      if (!(value instanceof Character) && !(value instanceof Boolean) &&
          !(value instanceof Integer) && !(value instanceof Float) && !(value instanceof String))
        throw new IllegalArgumentException("invalid value type: " +
            value.getClass().getSimpleName());
    }
    this.annotationId = annotationId;
    this.entityId = entityId;
    this.value = value;
  }
  
  public UUID getAnnotationId() {
    return annotationId;
  }
  
  public void setAnnotationId(@NotNull UUID annotationId) {
    this.annotationId = annotationId;
  }
  
  public int getEntityId() {
    return entityId;
  }
  
  public void setEntityId(int entityId) {
    if (entityId < 0) throw new IllegalArgumentException("negative id given.");
    this.entityId = entityId;
  }
  
  public Object getValue() {
    return value;
  }
  
  public void setValue(Object value) {
    if (value != null) {
      if (!(value instanceof Character) && !(value instanceof Boolean) &&
          !(value instanceof Integer) && !(value instanceof Float) && !(value instanceof String))
        throw new IllegalArgumentException("invalid value type: " +
            value.getClass().getSimpleName());
    }
    this.value = value;
  }
}
