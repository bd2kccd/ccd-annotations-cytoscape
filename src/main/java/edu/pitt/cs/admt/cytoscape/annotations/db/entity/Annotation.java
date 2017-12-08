package edu.pitt.cs.admt.cytoscape.annotations.db.entity;

import com.google.common.base.Preconditions;
import java.util.UUID;

/**
 * @author Nikos R. Katsipoulakis
 */
public class Annotation {

  private UUID id;

  private String description;

  public Annotation(UUID id, String description) {
    if (description != null) {
      Preconditions.checkArgument(description.length() <= 64);
    }
    this.id = id;
    this.description = description;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    if (description != null) {
      Preconditions.checkArgument(description.length() <= 64);
    }
    this.description = description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Annotation that = (Annotation) o;
    if (id != that.id) {
      return false;
    }
    return description.equals(that.description);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + description.hashCode();
    return result;
  }
}
