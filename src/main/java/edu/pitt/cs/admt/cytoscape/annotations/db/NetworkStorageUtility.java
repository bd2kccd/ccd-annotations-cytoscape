package edu.pitt.cs.admt.cytoscape.annotations.db;

import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Edge;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.ExtendedAttribute;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Node;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * @author Nikos R. Katsipoulakis
 */
public class NetworkStorageUtility {

  public static void importToDatabase(StorageDelegate storageDelegate,
      Collection<Node> nodes, Collection<Edge> edges,
      Collection<Annotation> annotations,
      Collection<ExtendedAttribute> attributes,
      Collection<AnnotToEntity> annotationToNode,
      Collection<AnnotToEntity> annotationToEdge)
      throws SQLException, IOException {
    if (storageDelegate == null)
      return;
    storageDelegate.insertNodes(nodes);
    storageDelegate.insertEdges(edges);
    storageDelegate.insertAnnotations(annotations);
    storageDelegate.insertAnnotationExtendedAttributes(attributes);
    for (AnnotToEntity e : annotationToNode) {
      storageDelegate.attachAnnotationToNode(e.getAnnotationId(), e.getEntityId(),
          e.getExtendedAttributeId(), e.getValue());
    }
    for (AnnotToEntity e : annotationToEdge) {
      storageDelegate.attachAnnotationToEdge(e.getAnnotationId(), e.getEntityId(),
          e.getExtendedAttributeId(), e.getValue());
    }
  }

  public static Optional<Collection<Node>> exportNodes(StorageDelegate delegate)
      throws SQLException {
    if (delegate != null)
      return delegate.getNodes();
    return Optional.empty();
  }

  public static Optional<Collection<Edge>> exportEdges(StorageDelegate delegate)
      throws SQLException {
    if (delegate != null)
      return delegate.getEdges();
    return Optional.empty();
  }

  public static Optional<Collection<Annotation>> exportAnnotations(StorageDelegate delegate)
      throws SQLException {
    if (delegate != null)
      return delegate.getAnnotations();
    return Optional.empty();
  }

  public static Optional<Collection<ExtendedAttribute>> exportExtendedAttributes(
      StorageDelegate delegate) throws SQLException {
    if (delegate != null)
      return delegate.getExtendedAttributes();
    return Optional.empty();
  }

  public static Optional<Collection<AnnotToEntity>> exportAnnotationToNodes(
      StorageDelegate delegate) throws SQLException, IOException, ClassNotFoundException {
    if (delegate != null)
      return delegate.getAnnotationsToNodes();
    return Optional.empty();
  }

  public static Optional<Collection<AnnotToEntity>> exportAnnotationToEdges(
      StorageDelegate delegate) throws SQLException, IOException, ClassNotFoundException {
    if (delegate != null)
      return delegate.getAnnotationsToEdges();
    return Optional.empty();
  }

  public static Optional<Collection<Node>> selectNodesWithExtendedAttributeName(
      StorageDelegate delegate, String name) throws SQLException {
    if (delegate != null)
      return delegate.selectNodesWithExtendedAttribute(name);
    return Optional.empty();
  }

  public static Optional<Collection<Edge>> selectEdgesWithExtendedAttribute(
      StorageDelegate delegate, String name) throws SQLException {
    if (delegate != null)
      return delegate.selectEdgesWithExtendedAttribute(name);
    return Optional.empty();
  }
}
