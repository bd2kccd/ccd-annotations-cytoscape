package edu.pitt.cs.admt.cytoscape.annotations.task;

import edu.pitt.cs.admt.cytoscape.annotations.db.NetworkStorageUtility;
import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegate;
import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegateFactory;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotationValueType;
import edu.pitt.cs.admt.cytoscape.annotations.ui.CreateAnnotationPanel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.xml.soap.Text;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class CreateAnnotationTask extends AbstractTask {

  // table panel column names
  private static final String CCD_ANNOTATION_ATTRIBUTE = "__CCD_Annotations";
  private static final String ANNOTATION_SET_ATTRIBUTE = "__CCD_Annotation_Set";

  // network table column
  private static final String COLUMN = "selected";

  // text annotation properties
  private static final Double ZOOM = 1.0;
  private static final int COLOR = -16777216;
  private static final String CANVAS = "foreground";
  private static final String FONT_FAMILY = "Arial";

  private final AnnotationManager annotationManager;
  private final AnnotationFactory<TextAnnotation> annotationFactory;
  private final CyNetwork network;
  private final CyNetworkView networkView;
  private final List<CyNode> nodes = new ArrayList<>(0);
  private final List<CyEdge> edges = new ArrayList<>(0);
  private final String annotationName;
  private UUID ccdAnnotationID = null;
  private UUID cytoscapeID = null;
  private String annotationDescription = "";
  private AnnotationValueType annotationValueType = null;
  private Object annotationValue = null;
  private boolean updateDB = false;

  protected CreateAnnotationTask(
      final CyApplicationManager applicationManager,
      final AnnotationManager annotationManager,
      final AnnotationFactory<TextAnnotation> annotationFactory,
      final String annotationName) {
    this.annotationManager = annotationManager;
    this.annotationFactory = annotationFactory;
    this.annotationName = annotationName;
    this.network = applicationManager.getCurrentNetwork();
    this.networkView = applicationManager.getCurrentNetworkView();
  }

  protected CreateAnnotationTask(
      final CyNetworkView networkView,
      final CyNetwork network,
      final AnnotationManager annotationManager,
      final AnnotationFactory<TextAnnotation> annotationFactory,
      final String annotationName) {
    this.networkView = networkView;
    this.network = network;
    this.annotationManager = annotationManager;
    this.annotationFactory = annotationFactory;
    this.annotationName = annotationName;
  }

  public static CreateAnnotationTask createAnnotationTaskOnSelected(
      final CyApplicationManager applicationManager,
      final AnnotationManager annotationManager,
      final AnnotationFactory<TextAnnotation> annotationFactory,
      final String annotationName) {
    CreateAnnotationTask task = new CreateAnnotationTask(applicationManager,
        annotationManager,
        annotationFactory,
        annotationName);
    task.nodes.addAll(CyTableUtil.getNodesInState(task.network, COLUMN, true));
    task.edges.addAll(CyTableUtil.getEdgesInState(task.network, COLUMN, true));
    return task;
  }

  // NOTE: Need static factory methods because constructors would have same type erasure
  public static CreateAnnotationTask createAnnotationTaskOnNodes(
      final CyApplicationManager applicationManager,
      final AnnotationManager annotationManager,
      final AnnotationFactory<TextAnnotation> annotationFactory,
      final String annotationName,
      final Collection<CyNode> nodes) {
    CreateAnnotationTask task = new CreateAnnotationTask(applicationManager,
        annotationManager,
        annotationFactory,
        annotationName);
    task.nodes.addAll(nodes);
    return task;
  }

  public static CreateAnnotationTask createAnnotationTaskOnEdges(
      final CyApplicationManager applicationManager,
      final AnnotationManager annotationManager,
      final AnnotationFactory<TextAnnotation> annotationFactory,
      final String annotationName,
      final Collection<CyEdge> edges) {
    CreateAnnotationTask task = new CreateAnnotationTask(applicationManager,
        annotationManager,
        annotationFactory,
        annotationName);
    task.edges.addAll(edges);
    return task;
  }

  public static CreateAnnotationTask createAnnotationTaskOnNodesAndEdges(
      final CyApplicationManager applicationManager,
      final AnnotationManager annotationManager,
      final AnnotationFactory<TextAnnotation> annotationFactory,
      final String annotationName,
      final Collection<CyNode> nodes,
      final Collection<CyEdge> edges) {
    CreateAnnotationTask task = new CreateAnnotationTask(applicationManager,
        annotationManager,
        annotationFactory,
        annotationName);
    task.nodes.addAll(nodes);
    task.edges.addAll(edges);
    return task;
  }

  public static CreateAnnotationTask onNodesAndEdges(
      final CyNetworkView networkView,
      final CyNetwork network,
      final AnnotationManager annotationManager,
      final AnnotationFactory<TextAnnotation> annotationFactory,
      final String annotationName,
      final Collection<CyNode> nodes,
      final Collection<CyEdge> edges) {
    CreateAnnotationTask task = new CreateAnnotationTask(
        networkView, network, annotationManager, annotationFactory, annotationName);
    task.nodes.addAll(nodes);
    task.edges.addAll(edges);
    return task;
  }

  public String getAnnotationName() {
    return annotationName;
  }

  public String getAnnotationDescription() {
    return annotationDescription;
  }

  public CreateAnnotationTask setAnnotationDescription(final String annotationDescription) {
    this.annotationDescription = annotationDescription;
    return this;
  }

  public UUID getCCDAnnotationID() {
    return ccdAnnotationID;
  }

  public CreateAnnotationTask setCCDAnnotationID(UUID uuid) {
    this.ccdAnnotationID = uuid;
    return this;
  }

  public UUID getCytoscapeID() {
    return cytoscapeID;
  }

  public CreateAnnotationTask setCytoscapeID(UUID uuid) {
    this.cytoscapeID = uuid;
    return this;
  }

  public Object getAnnotationValue() {
    return annotationValue;
  }

  public CreateAnnotationTask enableDatabaseUpdate() {
    this.updateDB = true;
    return this;
  }

  public CreateAnnotationTask setAnnotationValue(Object annotationValue) {
    this.annotationValue = annotationValue;
    return this;
  }

  public AnnotationValueType getAnnotationValueType() {
    return annotationValueType;
  }

  public CreateAnnotationTask setAnnotationValueType(final AnnotationValueType type) {
    this.annotationValueType = type;
    return this;
  }

  public TaskIterator createTaskIterator() {
    return new TaskIterator(this);
  }

  public void run(TaskMonitor monitor) throws MissingComponentsException {
    // Verify that there's something to annotate
    if (this.nodes.isEmpty() && this.edges.isEmpty()) {
      throw new MissingComponentsException();
    }
    if (this.ccdAnnotationID == null) {
      this.ccdAnnotationID = UUID.randomUUID();
    }
    if (this.cytoscapeID == null) {
      this.cytoscapeID = UUID.randomUUID();
    }

    // Set annotation parameters
    final Coordinates location = calculateAverageLocation();
    Map<String, String> args = new HashMap<>();
    args.put("x", String.valueOf(location.x));
    args.put("y", String.valueOf(location.y));
    args.put("zoom", String.valueOf(ZOOM));
    args.put("fontFamily", FONT_FAMILY);
    args.put("color", String.valueOf(COLOR));
    args.put("canvas", CANVAS);
    if (this.annotationValue != null) {
      args.put("text", this.annotationName + ": " + this.annotationValue.toString());
    } else {
      args.put("text", this.annotationName);
    }
    args.put("uuid", this.cytoscapeID.toString());


    // Create and add annotation to network
    TextAnnotation annotation = this.annotationFactory
        .createAnnotation(TextAnnotation.class, this.networkView, args);
    this.annotationManager.addAnnotation(annotation);
    updateNetworkTable(annotation);
    if (this.updateDB) {
      updateDatabase();
    }
  }

  private void updateDatabase() {
    Optional<StorageDelegate> storageDelegateOptional = StorageDelegateFactory.getDelegate(this.network.getSUID());
    if (storageDelegateOptional.isPresent()) {
      StorageDelegate delegate = storageDelegateOptional.get();
      try {
        System.out.println("Before: " + delegate.selectNodesWithAnnotation(this.annotationName).size() + delegate.selectEdgesWithAnnotation(this.annotationName).size());
        // make sure annotation with this name doesn't already exist
        Optional<Annotation> optionalAnnotation = delegate.getAnnotationByName(this.annotationName);
        if (!optionalAnnotation.isPresent()) {
          delegate.insertAnnotation(this.ccdAnnotationID, this.annotationName, this.annotationValueType, this.annotationDescription);
        }
        System.out.println("Creating annotation " + this.ccdAnnotationID + " on " + nodes.stream().map(CyNode::getSUID).collect(
            Collectors.toList()).toString());
        for (CyNode node: nodes) {
          delegate.attachAnnotationToNode(this.ccdAnnotationID, this.cytoscapeID, Math.toIntExact(node.getSUID()), this.annotationValue);
        }
        for (CyEdge edge: edges) {
          delegate.attachAnnotationToEdge(this.ccdAnnotationID, this.cytoscapeID, Math.toIntExact(edge.getSUID()), this.annotationValue);
        }
        System.out.println("After: " + delegate.selectNodesWithAnnotation(this.annotationName).size() + delegate.selectEdgesWithAnnotation(this.annotationName).size());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void updateNetworkTable(final TextAnnotation annotation) {
    // add to network
    List<String> row = this.network.getRow(this.network, CyNetwork.LOCAL_ATTRS)
        .getList(CCD_ANNOTATION_ATTRIBUTE, String.class);
    String annotationString = new StringBuilder()
        .append("uuid=").append(this.ccdAnnotationID.toString()).append("|")
        .append("name=").append(this.annotationName).append("|")
        .append("type=").append("float").append("|")
        .append("description=").append(this.annotationDescription).toString();
    row.add(annotationString);
    Set<String> rowSet = new HashSet<>(row);
    this.network.getRow(this.network, CyNetwork.LOCAL_ATTRS)
        .set(CCD_ANNOTATION_ATTRIBUTE, new ArrayList<>(rowSet));

    if (this.cytoscapeID == null) {
      this.cytoscapeID = annotation.getUUID();
      // add to node table
      for (CyNode node : this.nodes) {
        addToRow(node, this.ccdAnnotationID.toString(), this.cytoscapeID.toString());
      }

      // add to edge table
      for (CyEdge edge : this.edges) {
        addToRow(edge, this.ccdAnnotationID.toString(), this.cytoscapeID.toString());
      }
    }
  }

  private void addToRow(CyIdentifiable cyIdentifiable, String anUUID, String cyUUID) {
    List<String> row = this.network.getRow(cyIdentifiable)
        .getList(ANNOTATION_SET_ATTRIBUTE, String.class);
    String rowString = new StringBuilder()
        .append("a_id=").append(anUUID).append("|")
        .append("cy_id=").append(cyUUID).append("|")
        .append("value=").toString();
    if (annotationValue != null) {
      rowString = rowString + annotationValue.toString();
    }
    row.add(rowString);
    Set<String> rowSet = new HashSet<>(row);
    this.network.getRow(cyIdentifiable).set(ANNOTATION_SET_ATTRIBUTE, new ArrayList<>(rowSet));
  }

  private Coordinates calculateAverageLocation() {
    Double x = 0.0;
    Double y = 0.0;

    // Get x and y coordinates for nodes
    for (CyNode node : this.nodes) {
      final View<CyNode> cyNodeView = this.networkView.getNodeView(node);
      x += cyNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
      y += cyNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
    }

    // Get x and y coordinates for edges
    for (CyEdge edge : this.edges) {
      final View<CyNode> sourceView = this.networkView.getNodeView(edge.getSource());
      final View<CyNode> targetView = this.networkView.getNodeView(edge.getTarget());
      x += sourceView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
      x += targetView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
      y += sourceView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
      y += targetView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
    }

    /*
     * Calculate average per coordinate
     * Edge size is doubled to account for
     * both source and target nodes
     */
    x = x / (this.nodes.size() + this.edges.size() * 2);
    y = y / (this.nodes.size() + this.edges.size() * 2);

    return new Coordinates(x, y);
  }

  /**
   * Thrown when there are no graph components to annotate
   */
  public class MissingComponentsException extends Exception {

    public MissingComponentsException() {
      super("At least one graph component (node or edge) must be selected");
    }
  }

  /**
   * Coordinates pair
   * (x, y) tuple
   */
  private class Coordinates {

    public final Double x;
    public final Double y;

    public Coordinates(Double x, Double y) {
      this.x = x;
      this.y = y;
    }
  }
}
