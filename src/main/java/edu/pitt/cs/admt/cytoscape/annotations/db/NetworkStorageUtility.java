package edu.pitt.cs.admt.cytoscape.annotations.db;

import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Edge;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.ExtendedAttribute;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Node;
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
      Collection<ExtendedAttribute> attributes,
      Collection<AnnotToEntity> annotationToNode,
      Collection<AnnotToEntity> annotationToEdge)
      throws SQLException, IOException {
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
}
