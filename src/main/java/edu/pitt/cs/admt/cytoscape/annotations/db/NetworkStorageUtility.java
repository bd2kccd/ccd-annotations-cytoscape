package edu.pitt.cs.admt.cytoscape.annotations.db;

import edu.pitt.cs.admt.cytoscape.annotations.db.entity.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

/**
 * @author Nikos R. Katsipoulakis
 */
public class NetworkStorageUtility {
  public static void importToDatabase(StorageDelegate storageDelegate,
                                      Collection<Node> nodes, Collection<Edge> edges,
                                      Collection<Annotation> annotations,
                                      Collection<AnnotToEntity> annotationToNode,
                                      Collection<AnnotToEntity> annotationToEdge)
      throws SQLException, IOException {
    if (storageDelegate == null)
      return;
    storageDelegate.insertNodes(nodes);
    storageDelegate.insertEdges(edges);
    storageDelegate.insertAnnotations(annotations);
    for (AnnotToEntity e : annotationToNode)
      storageDelegate.attachAnnotationToNode(e.getAnnotationId(), e.getCytoscapeAnnotationId(),
          e.getEntityId(), e.getValue());
    for (AnnotToEntity e : annotationToEdge)
      storageDelegate.attachAnnotationToEdge(e.getAnnotationId(), e.getCytoscapeAnnotationId(),
          e.getEntityId(), e.getValue());
  }

  public static Collection<Node> exportNodes(StorageDelegate delegate)
      throws SQLException {
    if (delegate != null)
      return delegate.getNodes();
    return null;
  }

  public static Collection<Edge> exportEdges(StorageDelegate delegate)
      throws SQLException {
    if (delegate != null)
      return delegate.getEdges();
    return null;
  }

  public static Collection<Annotation> exportAnnotations(StorageDelegate delegate)
      throws SQLException {
    if (delegate != null)
      return delegate.getAllAnnotations();
    return null;
  }

  public static Collection<AnnotToEntity> exportAnnotationToNodes(
      StorageDelegate delegate) throws SQLException, IOException, ClassNotFoundException {
    if (delegate != null)
      return delegate.selectNodesWithAnnotation(null);
    return null;
  }

  public static Collection<AnnotToEntity> exportAnnotationToEdges(
      StorageDelegate delegate) throws SQLException, IOException, ClassNotFoundException {
    if (delegate != null)
      return delegate.selectEdgesWithAnnotation(null);
    return null;
  }

  public static Collection<AnnotToEntity> selectNodesWithAnnotationName(
      StorageDelegate delegate, String name) throws SQLException, IOException, ClassNotFoundException {
    if (delegate != null)
      return delegate.selectNodesWithAnnotation(name);
    return null;
  }

  public static Collection<AnnotToEntity> selectEdgesWithAnnotationName(
      StorageDelegate delegate, String name) throws SQLException, IOException, ClassNotFoundException {
    if (delegate != null)
      return delegate.selectEdgesWithAnnotation(name);
    return null;
  }
}
