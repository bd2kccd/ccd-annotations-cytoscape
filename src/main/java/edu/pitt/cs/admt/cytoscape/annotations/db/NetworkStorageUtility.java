package edu.pitt.cs.admt.cytoscape.annotations.db;

import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Edge;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Node;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

/**
 * @author Nikos R. Katsipoulakis
 */
public class NetworkStorageUtility {

  public static void importToDatabase(final long networkSUID,
      Collection<Node> nodes, Collection<Edge> edges,
      Collection<Annotation> annotations,
      Collection<AnnotToEntity> annotationToNode,
      Collection<AnnotToEntity> annotationToEdge)
      throws SQLException, IOException {
    StorageDelegate.insertNodes(networkSUID, nodes);
    StorageDelegate.insertEdges(networkSUID, edges);
    if (!annotations.isEmpty()) {
      StorageDelegate.insertAnnotations(networkSUID, annotations);
      for (AnnotToEntity e : annotationToNode) {
        StorageDelegate.attachAnnotationToNode(networkSUID, e.getAnnotationId(),
            e.getCytoscapeAnnotationId(), e.getEntityId(), e.getValue());
      }
      for (AnnotToEntity e : annotationToEdge) {
        StorageDelegate.attachAnnotationToEdge(networkSUID, e.getAnnotationId(),
            e.getCytoscapeAnnotationId(), e.getEntityId(), e.getValue());
      }
    }
  }

  public static Collection<Node> exportNodes(final long networkSUID)
      throws SQLException {
    return StorageDelegate.getNodes(networkSUID);
  }

  public static Collection<Edge> exportEdges(final long networkSUID)
      throws SQLException {
    return StorageDelegate.getEdges(networkSUID);
  }

  public static Collection<Annotation> exportAnnotations(final long networkSUID)
      throws SQLException {
    return StorageDelegate.getAllAnnotations(networkSUID);
  }

  public static Collection<AnnotToEntity> exportAnnotationToNodes(final long networkSUID) throws
      SQLException, IOException, ClassNotFoundException {
    return StorageDelegate.selectNodesWithAnnotation(networkSUID, null);
  }

  public static Collection<AnnotToEntity> exportAnnotationToEdges(final long networkSUID) throws
      SQLException, IOException, ClassNotFoundException {
    return StorageDelegate.selectEdgesWithAnnotation(networkSUID, null);
  }

  public static Collection<AnnotToEntity> selectNodesWithAnnotationName(
      final long networkSUID, String name) throws SQLException, IOException,
      ClassNotFoundException {
    return StorageDelegate.selectNodesWithAnnotation(networkSUID, name);
  }

  public static Collection<AnnotToEntity> selectEdgesWithAnnotationName(final long networkSUID,
                                                                        String name)
      throws SQLException, IOException, ClassNotFoundException {
    return StorageDelegate.selectEdgesWithAnnotation(networkSUID, name);
  }
}
