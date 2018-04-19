package edu.pitt.cs.admt.cytoscape.annotations.network;
import static edu.pitt.cs.admt.cytoscape.annotations.CCDAnnotation.CCD_ANNOTATION_SET;

import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegate;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Edge;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Node;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.AddedNodesListener;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class ComponentListener implements AddedNodesListener, AddedEdgesListener {

  @Override
  public void handleEvent(AddedNodesEvent event) {
    final CyNetwork network = event.getSource();
    final Long networkSUID = network.getSUID();
    final CyTable nodeTable = network.getDefaultNodeTable();

    // create annotation list
    event.getPayloadCollection()
        .forEach(node -> nodeTable.getRow(node.getSUID()).set(CCD_ANNOTATION_SET, new ArrayList<>(0)));

    // add to database
    Set<Node> nodes = event.getPayloadCollection()
        .stream()
        .map(CyNode::getSUID)
        .map(Long::intValue)
        .map(Node::new)
        .collect(Collectors.toSet());
    try {
      StorageDelegate.insertNodes(networkSUID, nodes);
    } catch (SQLException e) {
      System.out.println("Failed to add nodes");
      e.printStackTrace();
    }
  }

  @Override
  public void handleEvent(AddedEdgesEvent event) {
    final CyNetwork network = event.getSource();
    final Long networkSUID = network.getSUID();
    final CyTable edgeTable = network.getDefaultEdgeTable();

    // create annotation list
    event.getPayloadCollection()
        .forEach(edge -> edgeTable.getRow(edge.getSUID()).set(CCD_ANNOTATION_SET, new ArrayList<>(0)));

    // add to database
    Set<Edge> edges = event.getPayloadCollection()
        .stream()
        .map(e -> new Edge(
            e.getSUID().intValue(),
            e.getSource().getSUID().intValue(),
            e.getTarget().getSUID().intValue()))
        .collect(Collectors.toSet());
    try {
      StorageDelegate.insertEdges(networkSUID, edges);
    } catch (SQLException e) {
      System.out.println("Failed to add edges");
      e.printStackTrace();
    }
  }
}
