package edu.pitt.cs.admt.cytoscape.annotations.network;

import edu.pitt.cs.admt.cytoscape.annotations.db.NetworkStorageUtility;
import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegate;
import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegateFactory;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotationValueType;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Edge;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Node;
import edu.pitt.cs.admt.cytoscape.annotations.task.CreateAnnotationTask;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.cytoscape.work.TaskManager;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class NetworkListener implements NetworkViewAddedListener {

  private static final String CCD_ANNOTATION_ATTRIBUTE = "__CCD_Annotations";
  private static final String CCD_ANNOTATION_SET_ATTRIBUTE = "__CCD_Annotation_Set";
  private static final String ANNOTATION_ATTRIBUTE = "__Annotations";
  private final AnnotationManager annotationManager;
  private final AnnotationFactory<TextAnnotation> annotationFactory;
  private final TaskManager taskManager;

  // Type of graph component
  private enum ComponentType { NODE, EDGE };

  public NetworkListener(
      final AnnotationManager annotationManager,
      final AnnotationFactory<TextAnnotation> annotationFactory,
      final TaskManager taskManager) {
    this.annotationManager = annotationManager;
    this.annotationFactory = annotationFactory;
    this.taskManager = taskManager;
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
    Set<UUID> cytoscapeAnnotationUUIDs = this.getCytoscapeAnnotations(network);
    Map<UUID, Annotation> ccdAnnotationByUUID = this.getAnnotations(network);
    Collection<Annotation> annotations = ccdAnnotationByUUID.values();
    Map<ComponentType, List<AnnotToEntity>> annotationsByComponent = this.getAnnotationsForComponents(
        view, network, nodeTable, edgeTable, ccdAnnotationByUUID, cytoscapeAnnotationUUIDs);
    System.out.println("# node annos: " + annotationsByComponent.get(ComponentType.NODE).size());

    try {
      NetworkStorageUtility.importToDatabase(storageDelegate, nodes, edges,
          annotations, annotationsByComponent.get(ComponentType.NODE), annotationsByComponent.get(ComponentType.EDGE));
    } catch (Exception e) {
      System.out.println("Failed to import to database");
      e.printStackTrace();
    }
    System.out.println("Successfully imported annotations");

    System.out.println("Test");
    try {
      Collection<AnnotToEntity> test = NetworkStorageUtility.exportAnnotationToEdges(storageDelegate);
      Collection<Edge> test2 = NetworkStorageUtility.exportEdges(storageDelegate);
      Collection<AnnotToEntity> test3 = NetworkStorageUtility.exportAnnotationToNodes(storageDelegate);
      System.out.println("# edges: " +test2.size());
      System.out.println("# annotations to edges: " + test.size());
      for (AnnotToEntity a: test) {
        System.out.println(a.toString() + " SUID: " + a.getEntityId());
      }
      System.out.println("# annotations to nodes: " + test3.size());
    } catch (Exception e) {
      e.printStackTrace();
    }
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
        .map(this::parseCCDAnnotationString)
        .collect(Collectors.toMap(Annotation::getId, Function.identity()));
  }

  private Set<UUID> getCytoscapeAnnotations(final CyNetwork network) {
    return network.getRow(network, CyNetwork.LOCAL_ATTRS)
        .getList(ANNOTATION_ATTRIBUTE, String.class)
        .stream()
        .map(this::getUUIDFromCytoscapeAnnotationString)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
  }

  private Map<ComponentType, List<AnnotToEntity>> getAnnotationsForComponents(
      final CyNetworkView view,
      final CyNetwork network,
      final CyTable nodes,
      final CyTable edges,
      final Map<UUID, Annotation> ccdAnnotations,
      final Set<UUID> cytoscapeAnnotations) {
    Map<UUID, List<AnnotToEntity>> entityAnnotationByCcdID = new HashMap<>();
    Map<UUID, UUID> entityAnnotationGeneratedUUID = new HashMap<>();
    Map<UUID, List<AnnotToEntity>> entityAnnotationByCyID = new HashMap<>();
    // Prepare nodes
    for (CyRow row: nodes.getAllRows()) {
      Long suid = row.get("suid", Long.class);
      List<String> rowAnnos = row.getList(CCD_ANNOTATION_SET_ATTRIBUTE, String.class);
      for (int i = 0; i < rowAnnos.size(); i++) {
        AnnotToNode nodeAnno = new AnnotToNode(parseAnnotToEntityString(rowAnnos.get(i), suid, ccdAnnotations));
        UUID cyUUID;
        //noinspection Duplicates
        if (nodeAnno.getCytoscapeAnnotationId() == null) {
          if (!entityAnnotationByCcdID.containsKey(nodeAnno.getAnnotationId())) {
            entityAnnotationByCcdID.put(nodeAnno.getAnnotationId(), new ArrayList<>());
          }
          if (!entityAnnotationGeneratedUUID.containsKey(nodeAnno.getAnnotationId())) {
            cyUUID = UUID.randomUUID();
            entityAnnotationGeneratedUUID.put(nodeAnno.getAnnotationId(), cyUUID);
          } else {
            cyUUID = entityAnnotationGeneratedUUID.get(nodeAnno.getAnnotationId());
          }
          nodeAnno.setCytoscapeAnnotationId(cyUUID);
          entityAnnotationByCcdID.get(nodeAnno.getAnnotationId()).add(nodeAnno);
        } else {
          if (!entityAnnotationByCyID.containsKey(nodeAnno.getCytoscapeAnnotationId())) {
            entityAnnotationByCyID.put(nodeAnno.getCytoscapeAnnotationId(), new ArrayList<>());
          }
          cyUUID = nodeAnno.getCytoscapeAnnotationId();
          entityAnnotationByCyID.get(nodeAnno.getCytoscapeAnnotationId()).add(nodeAnno);
        }
        rowAnnos.set(i, nodeAnno.toString());
      }
      row.set(CCD_ANNOTATION_SET_ATTRIBUTE, rowAnnos);
    }

    // Prepare edges
    for (CyRow row: edges.getAllRows()) {
      Long suid = row.get("suid", Long.class);
      List<String> rowAnnos = row.getList(CCD_ANNOTATION_SET_ATTRIBUTE, String.class);
      for (int i = 0; i < rowAnnos.size(); i++) {
        AnnotToEdge edgeAnno = new AnnotToEdge(
            parseAnnotToEntityString(rowAnnos.get(i), suid, ccdAnnotations));
        UUID cyUUID;
        //noinspection Duplicates
        if (edgeAnno.getCytoscapeAnnotationId() == null) {
          if (!entityAnnotationByCcdID.containsKey(edgeAnno.getAnnotationId())) {
            entityAnnotationByCcdID.put(edgeAnno.getAnnotationId(), new ArrayList<>());
          }
          if (!entityAnnotationGeneratedUUID.containsKey(edgeAnno.getAnnotationId())) {
            cyUUID = UUID.randomUUID();
            entityAnnotationGeneratedUUID.put(edgeAnno.getAnnotationId(), cyUUID);
          } else {
            cyUUID = entityAnnotationGeneratedUUID.get(edgeAnno.getAnnotationId());
          }
          edgeAnno.setCytoscapeAnnotationId(cyUUID);
          entityAnnotationByCcdID.get(edgeAnno.getAnnotationId()).add(edgeAnno);
        } else {
          if (!entityAnnotationByCyID.containsKey(edgeAnno.getCytoscapeAnnotationId())) {
            entityAnnotationByCyID.put(edgeAnno.getCytoscapeAnnotationId(), new ArrayList<>());
          }
          cyUUID = edgeAnno.getCytoscapeAnnotationId();
          entityAnnotationByCyID.get(edgeAnno.getCytoscapeAnnotationId()).add(edgeAnno);
        }
        rowAnnos.set(i, edgeAnno.toString());
      }
      row.set(CCD_ANNOTATION_SET_ATTRIBUTE, rowAnnos);
    }

    // Iterate through CCD annotations UUIDs
    // and generate 1 Cytoscape annotation per entry
    for (Map.Entry<UUID, List<AnnotToEntity>> entry: entityAnnotationByCcdID.entrySet()) {
      UUID ccdUUID = entry.getKey();
      Annotation annotation = ccdAnnotations.get(ccdUUID);
      List<CyNode> nodesToAnnotate = entry.getValue()
          .stream()
          .filter(e -> e instanceof AnnotToNode)
          .map(AnnotToEntity::getEntityId)
          .map(e -> network.getNode(e))
          .collect(Collectors.toList());
      List<CyEdge> edgesToAnnotate = entry.getValue()
          .stream()
          .filter(e -> e instanceof AnnotToEdge)
          .map(AnnotToEntity::getEntityId)
          .map(e -> network.getEdge(e))
          .collect(Collectors.toList());

      this.taskManager.execute(
          CreateAnnotationTask.onNodesAndEdges(view, network, this.annotationManager, this.annotationFactory, annotation.getName(), nodesToAnnotate, edgesToAnnotate)
              .setAnnotationDescription(annotation.getDescription())
              .setCCDAnnotationID(ccdUUID)
              .setCytoscapeID(entityAnnotationGeneratedUUID.get(ccdUUID))
              .setAnnotationValue(entry.getValue().get(0).getValue())
              .createTaskIterator());
    }

    // Iterate through Cytoscape annotation UUIDs
    // and generate 1 Cytoscape annotatino per entry
    for (Map.Entry<UUID, List<AnnotToEntity>> entry: entityAnnotationByCyID.entrySet()) {
      UUID cyUUID = entry.getKey();
      if (!cytoscapeAnnotations.contains(cyUUID)) {
        Annotation annotation = ccdAnnotations.get(entry.getValue().get(0).getAnnotationId());
        List<CyNode> nodesToAnnotate = entry.getValue()
            .stream()
            .filter(e -> e instanceof AnnotToNode)
            .map(AnnotToEntity::getEntityId)
            .map(e -> network.getNode(e))
            .collect(Collectors.toList());
        List<CyEdge> edgesToAnnotate = entry.getValue()
            .stream()
            .filter(e -> e instanceof AnnotToEdge)
            .map(AnnotToEntity::getEntityId)
            .map(e -> network.getEdge(e))
            .collect(Collectors.toList());
        this.taskManager.execute(
            CreateAnnotationTask
                .onNodesAndEdges(view, network, this.annotationManager, this.annotationFactory,
                    annotation.getName(), nodesToAnnotate, edgesToAnnotate)
                .setAnnotationDescription(annotation.getDescription())
                .setCCDAnnotationID(annotation.getId())
                .setCytoscapeID(cyUUID)
                .setAnnotationValue(entry.getValue().get(0).getValue())
                .createTaskIterator());
      }
    }
    System.out.println("Type test: " + entityAnnotationByCcdID.values().stream().findAny().get().get(0).getClass().toString());

    Map<ComponentType, List<AnnotToEntity>> entityByType = new HashMap<>();
    entityByType.put(ComponentType.NODE, new ArrayList<>());
    entityByType.put(ComponentType.EDGE, new ArrayList<>());
    for (List<AnnotToEntity> l: entityAnnotationByCcdID.values()) {
      for (AnnotToEntity e: l) {
        if (e instanceof AnnotToNode) {
          entityByType.get(ComponentType.NODE).add(e);
        } else if (e instanceof AnnotToEdge) {
          entityByType.get(ComponentType.EDGE).add(e);
        }
      }
    }

    for (List<AnnotToEntity> l: entityAnnotationByCyID.values()) {
      for (AnnotToEntity e: l) {
        if (e instanceof  AnnotToNode) {
          entityByType.get(ComponentType.NODE).add(e);
        } else if (e instanceof AnnotToEdge) {
          entityByType.get(ComponentType.EDGE).add(e);
        }
      }
    }

    return entityByType;
  }

  private List<AnnotToEntity> getAnnotationsForNode(
      final CyNetworkView view,
      final CyNetwork network,
      final CyRow row,
      final Map<UUID, Annotation> ccdAnnotationByUUID) {
    final Long suid = row.get("suid", Long.class);
    final List<String> annoList = row.getList(CCD_ANNOTATION_SET_ATTRIBUTE, String.class);
    final List<AnnotToEntity> annotToEntities = new LinkedList<>();
    for (int i = 0; i < annoList.size(); i++) {
      AnnotToEntity annotToEntity = parseAnnotToEntityString(annoList.get(i), suid, ccdAnnotationByUUID);
      Annotation annotation = ccdAnnotationByUUID.get(annotToEntity.getAnnotationId());
      if (annotToEntity.getCytoscapeAnnotationId() == null) {
        CyNode node = network.getNode(suid);
        UUID cyId = this.createTextAnnotationOnNode(view, node,
            annotation.getName() + ": " + annotToEntity.getValue().toString());
        annotToEntity.setCytoscapeAnnotationId(cyId);
        annoList.set(i, annotToEntity.toString());
      }
      annotToEntities.add(annotToEntity);
    }
    row.set(CCD_ANNOTATION_SET_ATTRIBUTE, annoList);
    return annotToEntities;
  }

  private List<AnnotToEntity> getAnnotationsForEdge(
      final CyNetworkView view,
      final CyNetwork network,
      final CyRow row,
      final Map<UUID, Annotation> ccdAnnotationByUUID) {
    final Long suid = row.get("suid", Long.class);
    final List<String> annoList = row.getList(CCD_ANNOTATION_SET_ATTRIBUTE, String.class);
    final List<AnnotToEntity> annotToEntities = new LinkedList<>();
    for (int i = 0; i < annoList.size(); i++) {
      AnnotToEntity annotToEntity = parseAnnotToEntityString(annoList.get(i), suid, ccdAnnotationByUUID);
      Annotation annotation = ccdAnnotationByUUID.get(annotToEntity.getAnnotationId());
      if (annotToEntity.getCytoscapeAnnotationId() == null) {
        CyEdge edge = network.getEdge(suid);
        UUID cyId = this.createTextAnnotationOnEdge(view, edge,
            annotation.getName() + ": " + annotToEntity.getValue().toString());
        annotToEntity.setCytoscapeAnnotationId(cyId);
        annoList.set(i, annotToEntity.toString());
      }
      annotToEntities.add(annotToEntity);
    }
    row.set(CCD_ANNOTATION_SET_ATTRIBUTE, annoList);
    return annotToEntities;
  }

  private Optional<UUID> getUUIDFromCytoscapeAnnotationString(final String str) {
    for (String s: str.split("\\|")) {
      String[] field = s.split("=");
      if (field[0].equals("uuid")) {
        return Optional.of(UUID.fromString(field[1]));
      }
    }
    return Optional.empty();
  }

  private Annotation parseCCDAnnotationString(final String str) {
    String[] s = str.split("\\|");
    UUID uuid = UUID.fromString(s[0].split("=")[1]);
    String name = s[1].split("=")[1];
    AnnotationValueType type = AnnotationValueType.parse(s[2].split("=")[1].toUpperCase());
    String desc = s[3].split("=")[1];
    return new Annotation(uuid, name, type, desc);
  }

  private AnnotToEntity parseAnnotToEntityString(
      final String str,
      final Long suid,
      final Map<UUID, Annotation> ccdAnnotationByUUID) {
    String[] s = str.split("\\|");
    UUID annoId = UUID.fromString(s[0].split("=")[1]);
    Annotation annotation = ccdAnnotationByUUID.get(annoId);
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

  private UUID createTextAnnotationOnNode(
      final CyNetworkView view,
      final CyNode node,
      final String text) {
    View<CyNode> nodeView = view.getNodeView(node);
    Double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
    Double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
    TextAnnotation annotation = this.createTextAnnotation(view, x, y, text);
    this.annotationManager.addAnnotation(annotation);
    return annotation.getUUID();
  }

  private UUID createTextAnnotationOnEdge(
      final CyNetworkView view,
      final CyEdge edge,
      final String text) {
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

  private TextAnnotation createTextAnnotation(
      final CyNetworkView view,
      final Double x,
      final Double y,
      final String text) {
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

  private class AnnotToNode extends AnnotToEntity {
    private AnnotToNode(
        UUID annotationId,
        UUID cytoscapeAnnotationId,
        int entityId,
        Object value) {
      super(annotationId, cytoscapeAnnotationId, entityId, value);
    }

    private AnnotToNode(AnnotToEntity entity) {
      super(entity.getAnnotationId(), entity.getCytoscapeAnnotationId(), entity.getEntityId(), entity.getValue());
    }
  }

  private class AnnotToEdge extends AnnotToEntity {
    private AnnotToEdge(
        UUID annotationID,
        UUID cytoscapeAnnotationID,
        int entityID,
        Object value) {
      super(annotationID, cytoscapeAnnotationID, entityID, value);
    }

    private AnnotToEdge(AnnotToEntity entity) {
      super(entity.getAnnotationId(), entity.getCytoscapeAnnotationId(), entity.getEntityId(), entity.getValue());
    }
  }
}
