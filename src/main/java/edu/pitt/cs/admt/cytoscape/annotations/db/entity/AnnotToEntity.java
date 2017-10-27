package edu.pitt.cs.admt.cytoscape.annotations.db.entity;

import com.google.common.base.Preconditions;

import java.io.File;

/**
 * @author Nikos R. Katsipoulakis
 */
public class AnnotToEntity {
  
  private int annotationId;
  
  private int entityId;
  
  private Integer extendedAttributeId;
  
  private Object value;
  
  public AnnotToEntity(int annotationId, int entityId, Integer extendedAttributeId, Object value) {
    Preconditions.checkArgument(annotationId >= 0 && entityId >= 0);
    if (extendedAttributeId != null)
      Preconditions.checkArgument(extendedAttributeId >= 0);
    if (value != null)
      Preconditions.checkArgument(value instanceof Character || value instanceof Boolean ||
      value instanceof Integer || value instanceof Float || value instanceof String);
    this.annotationId = annotationId;
    this.entityId = entityId;
    this.extendedAttributeId = extendedAttributeId;
    this.value = value;
  }
  
  public int getAnnotationId() {
    return annotationId;
  }
  
  public void setAnnotationId(int annotationId) {
    Preconditions.checkArgument(annotationId >= 0);
    this.annotationId = annotationId;
  }
  
  public int getEntityId() {
    return entityId;
  }
  
  public void setEntityId(int entityId) {
    Preconditions.checkArgument(entityId >= 0);
    this.entityId = entityId;
  }
  
  public Integer getExtendedAttributeId() {
    return extendedAttributeId;
  }
  
  public void setExtendedAttributeId(Integer extendedAttributeId) {
    if (extendedAttributeId != null)
      Preconditions.checkArgument(extendedAttributeId >= 0);
    this.extendedAttributeId = extendedAttributeId;
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
