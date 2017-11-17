package edu.pitt.cs.admt.cytoscape.annotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.pitt.cs.admt.cytoscape.annotations.db.NetworkStorageUtility;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Edge;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.ExtendedAttribute;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.ExtendedAttributeType;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Node;
import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegate;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class NetworkListener implements NetworkAddedListener {

    private static final String CCD_ANNOTATION_ATTRIBUTE = "__CCD_Annotations";
    private static final String ANNOTATION_SET_ATTRIBUTE = "__Annotation_Set";
    private static final String ANNOTATION_ATTRIBUTE="__Annotations";

    public void handleEvent(final NetworkAddedEvent networkAddedEvent) {
        final CyNetwork network = networkAddedEvent.getNetwork();
        final CyTable networkTable = network.getDefaultNetworkTable();
        final CyTable nodeTable = network.getDefaultNodeTable();
        final CyTable edgeTable = network.getDefaultEdgeTable();

        System.out.println("This should be all nodes:\n");
        System.out.println(CyTableUtil.getNodesInState(network, "selected", false).toString());

        // generate columns
        if (networkTable.getColumn(CCD_ANNOTATION_ATTRIBUTE) == null) {
            System.out.println("Creating network column: " + CCD_ANNOTATION_ATTRIBUTE);
            networkTable.createListColumn(CCD_ANNOTATION_ATTRIBUTE, String.class, false, new ArrayList<>(0));
        }

        if (nodeTable.getColumn(ANNOTATION_SET_ATTRIBUTE) == null) {
            System.out.println("Creating node column: " + ANNOTATION_SET_ATTRIBUTE);
            nodeTable.createListColumn(ANNOTATION_SET_ATTRIBUTE, String.class, false, new ArrayList<>(0));
            System.out.println("Testing");
            CyColumn column = nodeTable.getColumn("name");
            System.out.println("Values: " + column.getValues(String.class).toString());
        }

        if (edgeTable.getColumn(ANNOTATION_SET_ATTRIBUTE) == null) {
            System.out.println("Creating edge column: " + ANNOTATION_SET_ATTRIBUTE);
            edgeTable.createListColumn(ANNOTATION_SET_ATTRIBUTE, String.class, false, new ArrayList<>(0));
        }

        this.importToDatabase(network);
    }

    private void importToDatabase(final CyNetwork cyNetwork) {
        // Get storage delegate
        StorageDelegate storageDelegate = new StorageDelegate();

        // No nodes and edges at this point should be selected
        // get list of all nodes and edges
        List<CyNode> cyNodes = CyTableUtil.getNodesInState(cyNetwork, "selected", false);
        List<CyEdge> cyEdges = CyTableUtil.getEdgesInState(cyNetwork, "selected", false);

        // map to entities
        // get list of nodes
        List<Node> nodes = cyNodes
                .stream()
                .map(CyNode::getSUID)
                .map(Math::toIntExact)
                .map(Node::new)
                .collect(Collectors.toList());

        // get list of edges
        List<Edge> edges = cyEdges
                .stream()
                .map(edge -> {
                    int suid = Math.toIntExact(edge.getSUID());
                    int source = Math.toIntExact(edge.getSource().getSUID());
                    int dest = Math.toIntExact(edge.getTarget().getSUID());
                    return new Edge(suid, source, dest);})
                .collect(Collectors.toList());

        // get list of CCD annotations
        List<String> cyAnnotations = cyNetwork.getRow(cyNetwork, CyNetwork.LOCAL_ATTRS).getList(ANNOTATION_ATTRIBUTE, String.class);
        System.out.println("Annotations: " + cyAnnotations.toString());
        List<String> texts = new ArrayList<>(0);
        if (cyAnnotations != null) {
            texts = cyAnnotations
                    .stream()
                    .map(str -> {
                        String text = str.substring(str.indexOf("text="));
                        return text.substring(5, text.indexOf("|")); })
                    .collect(Collectors.toList());

//            for (String s: cyAnnotations) {
//                int index = s.indexOf("text=");
//                String s2 = s.substring(index);
//                int barIndex = s2.indexOf("|");
//                String text = s2.substring(5, barIndex);
//                System.out.println(text);
//                texts.add(text);
//            }
        }

        List<Annotation> annotations = new ArrayList<>(0);
        for (int i = 0; i < texts.size(); i++) {
            annotations.add(new Annotation(i, texts.get(i)));
        }

        // get extended attributes
        List<ExtendedAttribute> extendedAttributes = new ArrayList<>(0);
        extendedAttributes.add(
                new ExtendedAttribute(1, "Posterior Probabilities", ExtendedAttributeType.FLOAT));
        extendedAttributes.add(
                new ExtendedAttribute(2, "Comment", ExtendedAttributeType.STRING));

        // get annotation to entity mappings
        System.out.println("Getting annotation to entity mappings");

        // annotation to node mapping
        System.out.println("Node annotations");
        CyTable nodeTable = cyNetwork.getDefaultNodeTable();
        List<CyRow> rows = nodeTable.getAllRows();
        for (CyRow row: rows) {
            System.out.println("Row");
            if (row.isSet("suid")) {
                System.out.println("SUID: " + row.getRaw("suid"));
            } else {
                System.out.println("suid column not found");
            }
            if (row.isSet("name")) {
                System.out.println("Name: " + row.get("name", String.class));
            } else {
                System.out.println("name column not found");
            }
            if (row.isSet(ANNOTATION_SET_ATTRIBUTE)) {
                System.out.println("Annotation set: " + row.get(ANNOTATION_SET_ATTRIBUTE, List.class).toString());
            }
            System.out.println();
        }

        // annotation to edge mapping
        System.out.println("Edge annotations");
        CyTable edgeTable = cyNetwork.getDefaultEdgeTable();
        rows = edgeTable.getAllRows();
        for (CyRow row: rows) {
            System.out.println("Row");
            if (row.isSet("suid")) {
                System.out.println("SUID: " + row.getRaw("suid"));
            } else {
                System.out.println("suid column not found");
            }
            if (row.isSet("name")) {
                System.out.println("Name: " + row.get("name", String.class));
            } else {
                System.out.println("name column not found");
            }
            if (row.isSet(ANNOTATION_SET_ATTRIBUTE)) {
                System.out.println("Annotation set: " + row.get(ANNOTATION_SET_ATTRIBUTE, List.class).toString());
            }
            System.out.println();
        }

        try {
            storageDelegate.init("Cytoscape");
            NetworkStorageUtility.importToDatabase(storageDelegate,
                    nodes, edges, annotations, extendedAttributes,
                    Collections.emptyList(), Collections.emptyList());
            System.out.println("Database loaded");
        } catch (Exception e) {
            System.out.println("Database load failed");
            e.printStackTrace();
        }
    }
}