package edu.pitt.cs.admt.cytoscape.annotations.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

    private final CyApplicationManager applicationManager;
    private final AnnotationManager annotationManager;
    private final AnnotationFactory<TextAnnotation> annotationFactory;
    private final CyNetwork network;
    private final CyNetworkView networkView;
    private final Collection<CyNode> nodes = new ArrayList<>(0);
    private final Collection<CyEdge> edges = new ArrayList<>(0);
    private String annotationName;
    private String annotationDescription = "";

    protected CreateAnnotationTask(final CyApplicationManager applicationManager,
                                   final AnnotationManager annotationManager,
                                   final AnnotationFactory<TextAnnotation> annotationFactory,
                                   final String annotationName) {
        this.applicationManager = applicationManager;
        this.annotationManager = annotationManager;
        this.annotationFactory = annotationFactory;
        this.annotationName = annotationName;
        this.network = this.applicationManager.getCurrentNetwork();
        this.networkView = this.applicationManager.getCurrentNetworkView();
    }

    public static CreateAnnotationTask createAnnotationTaskOnSelected(final CyApplicationManager applicationManager,
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
    public static CreateAnnotationTask createAnnotationTaskOnNodes(final CyApplicationManager applicationManager,
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

    public static CreateAnnotationTask createAnnotationTaskOnEdges(final CyApplicationManager applicationManager,
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

    public static CreateAnnotationTask createAnnotationTaskOnNodesAndEdges(final CyApplicationManager applicationManager,
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

    public String getAnnotationName() {
        return annotationName;
    }

    public void setAnnotationName(final String annotationName) {
        this.annotationName = annotationName;
    }

    public String getAnnotationDescription() {
        return annotationDescription;
    }

    public void setAnnotationDescription(final String annotationDescription) {
        this.annotationDescription = annotationDescription;
    }

    public void run(TaskMonitor monitor) throws MissingComponentsException {
        // verify that there's something to annotate
        if (this.nodes.isEmpty() && this.edges.isEmpty()) {
            throw new MissingComponentsException();
        }

        // set annotation parameters
        final Coordinates location = calculateAverageLocation();
        Map<String, String> args = new HashMap<>();
        args.put("x", String.valueOf(location.x));
        args.put("y", String.valueOf(location.y));
        args.put("zoom", String.valueOf(ZOOM));
        args.put("fontFamily", FONT_FAMILY);
        args.put("color", String.valueOf(COLOR));
        args.put("canvas", CANVAS);
        args.put("text", this.annotationName);

        // create and add annotation to network
        TextAnnotation annotation = this.annotationFactory.createAnnotation(TextAnnotation.class, this.networkView, args);
        this.annotationManager.addAnnotation(annotation);
        updateNetworkTable(annotation);
    }

    private void updateNetworkTable(final TextAnnotation annotation) {
        final String anUUID = UUID.randomUUID().toString();
        final String cyUUID = annotation.getUUID().toString();

        // add to network
        List<String> row = this.network.getRow(this.network, CyNetwork.LOCAL_ATTRS).getList(CCD_ANNOTATION_ATTRIBUTE, String.class);
        String annotationString = new StringBuilder()
                .append("uuid=").append(anUUID).append("|")
                .append("name=").append(this.annotationName).append("|")
                .append("type=").append("float").append("|")
                .append("description=").append(this.annotationDescription).toString();
        row.add(annotationString);
        this.network.getRow(this.network, CyNetwork.LOCAL_ATTRS).set(CCD_ANNOTATION_ATTRIBUTE, row);

        // add to node table
        for (CyNode node: this.nodes) {
            addToRow(node, anUUID, cyUUID);
        }

        // add to edge table
        for (CyEdge edge: this.edges) {
            addToRow(edge, anUUID, cyUUID);
        }
    }

    private void addToRow(CyIdentifiable cyIdentifiable, String anUUID, String cyUUID) {
        List<String> row = this.network.getRow(cyIdentifiable).getList(ANNOTATION_SET_ATTRIBUTE, String.class);
        String rowString = new StringBuilder()
                .append("a_id=").append(anUUID).append("|")
                .append("cy_id=").append(cyUUID).append("|")
                .append("value=").append(String.valueOf(0.8)).toString();
        row.add(rowString);
        Set<String> rowSet = new HashSet<>(row);
        this.network.getRow(cyIdentifiable).set(ANNOTATION_SET_ATTRIBUTE, new ArrayList<>(rowSet));
    }

    private Coordinates calculateAverageLocation() {
        Double x = 0.0;
        Double y = 0.0;

        // get x and y coordinates
        for (CyNode node: this.nodes) {
            final View<CyNode> cyNodeView = this.networkView.getNodeView(node);
            x += cyNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
            y += cyNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
        }

        for (CyEdge edge: this.edges) {
            final View<CyNode> sourceView = this.networkView.getNodeView(edge.getSource());
            final View<CyNode> targetView = this.networkView.getNodeView(edge.getTarget());
            x += sourceView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
            x += targetView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
            y += sourceView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
            y += targetView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
        }

        // calculate average
        // double edge count for both source and target nodes
        x = x/(this.nodes.size() + this.edges.size()*2);
        y = y/(this.nodes.size() + this.edges.size()*2);

        return new Coordinates(x, y);
    }

    public class MissingComponentsException extends Exception {
        public MissingComponentsException() {
            super("At least one graph component (node or edge) must be selected");
        }
    }

    public class Coordinates {
        public final Double x;
        public final Double y;

        public Coordinates(Double x, Double y) {
            this.x = x;
            this.y = y;
        }
    }
}
