package edu.pitt.cs.admt.cytoscape.annotations.network;

import edu.pitt.cs.admt.cytoscape.annotations.db.NetworkStorageUtility;
import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegate;
import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegateFactory;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotationValueType;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Edge;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Node;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class NetworkListener implements NetworkViewAddedListener {

  private static final String CCD_ANNOTATION_ATTRIBUTE = "__CCD_Annotations";
  private static final String CCD_ANNOTATION_SET_ATTRIBUTE = "__CCD_Annotation_Set";
  private static final String ANNOTATION_ATTRIBUTE = "__Annotations";
  private final AnnotationManager annotationManager;
  private final AnnotationFactory<TextAnnotation> annotationFactory;

  public NetworkListener(
      final AnnotationManager annotationManager,
      final AnnotationFactory<TextAnnotation> annotationFactory) {
    this.annotationManager = annotationManager;
    this.annotationFactory = annotationFactory;
  }

  public void handleEvent(final NetworkViewAddedEvent event) {
    final CyNetworkView view = event.getNetworkView();
    final CyNetwork network = view.getModel();
    final CyTable networkTable = network.getDefaultNetworkTable();
    final CyTable nodeTable = network.getDefaultNodeTable();
    final CyTable edgeTable = network.getDefaultEdgeTable();
    final StorageDelegate storageDelegate = StorageDelegateFactory.newDelegate(network.getSUID());

    // Generate columns in table panel
    if (networkTable.getColumn(CCD_ANNOTATION_ATTRIBUTE) == null) {
      networkTable.createListColumn(CCD_ANNOTATION_ATTRIBUTE, String.class, false);
    }
    if (nodeTable.getColumn(CCD_ANNOTATION_SET_ATTRIBUTE) == null) {
      nodeTable.createListColumn(CCD_ANNOTATION_SET_ATTRIBUTE, String.class, false);
    }
    if (edgeTable.getColumn(CCD_ANNOTATION_SET_ATTRIBUTE) == null) {
      edgeTable.createListColumn(CCD_ANNOTATION_SET_ATTRIBUTE, String.class, false);
    }

    // Connect to database
    try {
      storageDelegate.init();
      System.out.println("Initialized storage delegate");
    } catch (SQLException e) {
      System.out.println("Failed to initialize storage delegate");
      e.printStackTrace();
      return;
    }

    List<Node> nodes = this.getNodes(network);
    List<Edge> edges = this.getEdges(network);
    Map<UUID, Annotation> annotationMap = this.getAnnotations(network);
    Collection<Annotation> annotations = annotationMap.values();
    List<AnnotToEntity> annotationsToNode = nodeTable.getAllRows()
        .stream()
        .flatMap(row -> this.getAnnotationsForNode(view, network, row, annotationMap).stream())
        .collect(Collectors.toList());
    List<AnnotToEntity> annotationsToEdge = edgeTable.getAllRows()
        .stream()
        .flatMap(row -> this.getAnnotationsForEdge(view, network, row, annotationMap).stream())
        .collect(Collectors.toList());

    try {
      NetworkStorageUtility.importToDatabase(storageDelegate, nodes, edges, annotations, annotationsToNode, annotationsToEdge);
    } catch (Exception e) {
      System.out.println("Failed to import to database");
      e.printStackTrace();
    }
    System.out.println("Successfully imported annotations");
  }

  private List<Node> getNodes(final CyNetwork network) {
    // Get the list of all nodes
    // and convert to our entity
    return network.getNodeList()
        .stream()
        .map(CyNode::getSUID)
        .map(Math::toIntExact)
        .map(Node::new)
        .collect(Collectors.toList());
  }

  private List<Edge> getEdges(final CyNetwork network) {
    // Get the list of all edges
    // and convert to our entity
    return network.getEdgeList()
        .stream()
        .map(edge -> {
          int suid = Math.toIntExact(edge.getSUID());
          int source = Math.toIntExact(edge.getSource().getSUID());
          int target = Math.toIntExact(edge.getTarget().getSUID());
          return new Edge(suid, source, target);
        })
        .collect(Collectors.toList());
  }

  private Map<UUID, Annotation> getAnnotations(final CyNetwork network) {
    return network.getRow(network, CyNetwork.LOCAL_ATTRS)
        .getList(CCD_ANNOTATION_ATTRIBUTE, String.class)
        .stream()
        .map(this::parseAnnotationString)
        .collect(Collectors.toMap(Annotation::getId, Function.identity()));
  }

  private List<AnnotToEntity> getAnnotationsForNode(final CyNetworkView view,
                                                    final CyNetwork network,
                                                    final CyRow row,
                                                    final Map<UUID, Annotation> annotationMap) {
    final Long suid = row.get("suid", Long.class);
    final List<String> annoList = row.getList(CCD_ANNOTATION_SET_ATTRIBUTE, String.class);
    final List<AnnotToEntity> annotToEntities = new LinkedList<>();
    for (int i = 0; i < annoList.size(); i++) {
      AnnotToEntity annotToEntity = parseAnnotToEntityString(annoList.get(i), suid, annotationMap);
      Annotation annotation = annotationMap.get(annotToEntity.getAnnotationId());
      if (annotToEntity.getCytoscapeAnnotationId() == null) {
        CyNode node = network.getNode(suid);
        UUID cyId = this.createTextAnnotationOnNode(view, node, annotation.getName() + ": " + annotToEntity.getValue().toString());
        annotToEntity.setCytoscapeAnnotationId(cyId);
        annoList.set(i, annotToEntity.toString());
      }
      annotToEntities.add(annotToEntity);
    }
    row.set(CCD_ANNOTATION_SET_ATTRIBUTE, annoList);
    return annotToEntities;
  }

  private List<AnnotToEntity> getAnnotationsForEdge(final CyNetworkView view,
                                                    final CyNetwork network,
                                                    final CyRow row,
                                                    final Map<UUID, Annotation> annotationMap) {
    final Long suid = row.get("suid", Long.class);
    final List<String> annoList = row.getList(CCD_ANNOTATION_SET_ATTRIBUTE, String.class);
    final List<AnnotToEntity> annotToEntities = new LinkedList<>();
    for (int i = 0; i < annoList.size(); i++) {
      AnnotToEntity annotToEntity = parseAnnotToEntityString(annoList.get(i), suid, annotationMap);
      Annotation annotation = annotationMap.get(annotToEntity.getAnnotationId());
      if (annotToEntity.getCytoscapeAnnotationId() == null) {
        CyEdge edge = network.getEdge(suid);
        UUID cyId = this.createTextAnnotationOnEdge(view, edge, annotation.getName() + ": " + annotToEntity.getValue().toString());
        annotToEntity.setCytoscapeAnnotationId(cyId);
        annoList.set(i, annotToEntity.toString());
      }
      annotToEntities.add(annotToEntity);
    }
    row.set(CCD_ANNOTATION_SET_ATTRIBUTE, annoList);
    return annotToEntities;
  }

  private Annotation parseAnnotationString(final String str) {
    String[] s = str.split("\\|");
    UUID uuid = UUID.fromString(s[0].split("=")[1]);
    String name = s[1].split("=")[1];
    AnnotationValueType type = AnnotationValueType.parse(s[2].split("=")[1].toUpperCase());
    String desc = s[3].split("=")[1];
    return new Annotation(uuid, name, type, desc);
  }

  private AnnotToEntity parseAnnotToEntityString(final String str, final Long suid, final Map<UUID, Annotation> annotationMap) {
    String[] s = str.split("\\|");
    UUID annoId = UUID.fromString(s[0].split("=")[1]);
    Annotation annotation = annotationMap.get(annoId);
    AnnotationValueType type = annotation.getType();
    String[] value = s[2].split("=");
    AnnotToEntity annotToEntity = new AnnotToEntity(annoId, null, Math.toIntExact(suid), null);
    if (value.length > 1) {
      switch (type) {
        case INT:
          annotToEntity.setValue(Integer.parseInt(value[1]));
          break;
        case CHAR:
          annotToEntity.setValue(value[1].charAt(0));
          break;
        case FLOAT:
          annotToEntity.setValue(Float.parseFloat(value[1]));
          break;
        case BOOLEAN:
          annotToEntity.setValue(Boolean.parseBoolean(value[1]));
          break;
        case STRING:
          annotToEntity.setValue(value[1]);
          break;
        default:
          break;
      }
    }
    UUID cyId = null;
    String[] cyIdString = s[1].split("=");
    if (cyIdString.length > 1) {
      cyId = UUID.fromString(cyIdString[1]);
    }
    annotToEntity.setCytoscapeAnnotationId(cyId);
    return annotToEntity;
  }

  private UUID createTextAnnotationOnNode(final CyNetworkView view, final CyNode node, final String text) {
    View<CyNode> nodeView = view.getNodeView(node);
    Double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
    Double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
    TextAnnotation annotation = this.createTextAnnotation(view, x, y, text);
    this.annotationManager.addAnnotation(annotation);
    return annotation.getUUID();
  }

  private UUID createTextAnnotationOnEdge(final CyNetworkView view, final CyEdge edge, final String text) {
    View<CyNode> source = view.getNodeView(edge.getSource());
    View<CyNode> target = view.getNodeView(edge.getTarget());

    // get location
    Double x = 0.0, y = 0.0;
    x += source.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
    x += target.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
    x /= 2.0;
    y += source.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
    y += target.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
    y /= 2.0;
    
    TextAnnotation annotation = this.createTextAnnotation(view, x, y, text);
    this.annotationManager.addAnnotation(annotation);
    return annotation.getUUID();
  }

  private TextAnnotation createTextAnnotation(final CyNetworkView view, final Double x, final Double y, final String text) {
    Map<String, String> args = new HashMap<>(0);
    args.put("x", String.valueOf(x));
    args.put("y", String.valueOf(y));
    args.put("zoom", String.valueOf(1.0));
    args.put("fontFamily", "Arial");
    args.put("color", String.valueOf(-16777216));
    args.put("canvas", "foreground");
    args.put("text", text);
    return this.annotationFactory.createAnnotation(TextAnnotation.class, view, args);
  }
}
