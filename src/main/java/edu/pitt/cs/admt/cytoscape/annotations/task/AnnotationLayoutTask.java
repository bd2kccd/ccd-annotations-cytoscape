package edu.pitt.cs.admt.cytoscape.annotations.task;

import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegate;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class AnnotationLayoutTask extends AbstractTask {

  public enum LayoutStrategy {
    AVERAGE
  }

  private final AnnotationManager annotationManager;
  private final CyNetworkView view;
  private final LayoutStrategy strategy;
  private final Collection<Integer> nodes;
  private final Collection<Integer> edges;

  private AnnotationLayoutTask(
      final AnnotationManager annotationManager,
      final CyNetworkView view,
      final LayoutStrategy strategy,
      final Collection<Integer> nodes,
      final Collection<Integer> edges) {
    this.annotationManager = annotationManager;
    this.view = view;
    this.strategy = strategy;
    this.nodes = nodes;
    this.edges = edges;
  }

  private AnnotationLayoutTask(
      final AnnotationManager annotationManager,
      final CyNetworkView view,
      final LayoutStrategy strategy) {
    this(annotationManager, view, strategy, Collections.emptySet(), Collections.emptySet());
  }

  public static AnnotationLayoutTask CreateAnnotationLayoutTask(
      final AnnotationManager annotationManager,
      final CyNetworkView view,
      final LayoutStrategy layoutStrategy,
      final Collection<Integer> nodes,
      final Collection<Integer> edges) {
    return new AnnotationLayoutTask(annotationManager, view, layoutStrategy, nodes, edges);
  }

  public static AnnotationLayoutTask CreateAnnotationLayoutTask(
      final AnnotationManager annotationManager,
      final CyNetworkView view,
      final Collection<Integer> nodes,
      final Collection<Integer> edges) {
    return new AnnotationLayoutTask(annotationManager, view, LayoutStrategy.AVERAGE, nodes, edges);
  }

  public static AnnotationLayoutTask CreateAnnotationLayoutTask(
      final AnnotationManager annotationManager,
      final CyNetworkView view,
      final LayoutStrategy strategy) {
    return new AnnotationLayoutTask(annotationManager, view, strategy);
  }

  public static AnnotationLayoutTask CreateAnnotationLayoutTask(
      final AnnotationManager annotationManager,
      final CyNetworkView view) {
    return CreateAnnotationLayoutTask(annotationManager, view, LayoutStrategy.AVERAGE);
  }

  public TaskIterator toTaskIterator() {
    return new TaskIterator(this);
  }

  @Override
  public void run(TaskMonitor monitor) {
    switch (this.strategy) {
      case AVERAGE:
      default:
        averagePositionLayout(monitor);
        break;
    }
  }

  public void run() {
    switch (this.strategy) {
      case AVERAGE:
      default:
        averagePositionLayout();
        break;
    }
  }

  private void averagePositionLayout() {
    final Long network = this.view.getModel().getSUID();
    List<Annotation> annotations = this.annotationManager.getAnnotations(this.view);

    // filter out annotations that we don't want to relayout
    if (!nodes.isEmpty() || !edges.isEmpty()) {
      final Set<UUID> cyIDs = new HashSet<>();
      try {
        cyIDs.addAll(StorageDelegate.getAnnotationToEntities(network, nodes, edges)
            .stream().map(AnnotToEntity::getCytoscapeAnnotationId).collect(Collectors.toSet()));
      } catch (SQLException | IOException | ClassNotFoundException e) {
        System.out.println("Failed to fetch AnnotToEntity union");
        e.printStackTrace();
      }
      if (!cyIDs.isEmpty()) {
        annotations.removeIf(a -> !cyIDs.contains(a.getUUID()));
      }
    }

    // relayout annotations
    for (int i = 0; i < annotations.size(); i++) {
      Annotation annotation = annotations.get(i);
      UUID cyId = annotation.getUUID();
      try {
        Collection<Integer> components = StorageDelegate
            .getNetworkComponentsOnCytoscapeAnnotUUID(network, cyId);
        if (components.size() > 0) {
          Point2D location = averagePosition(components);
          annotation.moveAnnotation(location);
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  private void averagePositionLayout(final TaskMonitor monitor) {
    final Long network = this.view.getModel().getSUID();
    List<Annotation> annotations = this.annotationManager.getAnnotations(this.view);

    // filter out annotations that we don't want to relayout
    if (!nodes.isEmpty() || !edges.isEmpty()) {
      final Set<UUID> cyIDs = new HashSet<>();
      try {
        cyIDs.addAll(StorageDelegate.getAnnotationToEntities(network, nodes, edges)
            .stream().map(AnnotToEntity::getCytoscapeAnnotationId).collect(Collectors.toSet()));
      } catch (SQLException | IOException | ClassNotFoundException e) {
        System.out.println("Failed to fetch AnnotToEntity union");
        e.printStackTrace();
      }
      if (!cyIDs.isEmpty()) {
        annotations.removeIf(a -> !cyIDs.contains(a.getUUID()));
      }
    }

    // relayout annotations
    for (int i = 0; i < annotations.size(); i++) {
      Annotation annotation = annotations.get(i);
      UUID cyId = annotation.getUUID();
      try {
        Collection<Integer> components = StorageDelegate
            .getNetworkComponentsOnCytoscapeAnnotUUID(network, cyId);
        if (components.size() > 0) {
          Point2D location = averagePosition(components);
          annotation.moveAnnotation(location);
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
      monitor.setProgress((double) ((i + 1) / annotations.size()));
    }
  }

  private Point2D averagePosition(Collection<Integer> suids) {
    final CyNetwork network = this.view.getModel();
    final List<Double> x = new ArrayList<>();
    final List<Double> y = new ArrayList<>();

    suids.forEach(suid -> {
      CyNode node = network.getNode(suid.longValue());
      CyEdge edge = network.getEdge(suid.longValue());

      if (node != null) {
        View<CyNode> nodeView = this.view.getNodeView(node);
        x.add(nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION));
        y.add(nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));
      } else if (edge != null) {
        View<CyNode> sourceView = this.view.getNodeView(edge.getSource());
        View<CyNode> targetView = this.view.getNodeView(edge.getTarget());
        x.add(sourceView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION));
        y.add(sourceView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));
        x.add(targetView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION));
        y.add(targetView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));
      }
    });

    Double avgX = x.stream().mapToDouble(a -> a).average().getAsDouble();
    Double avgY = y.stream().mapToDouble(a -> a).average().getAsDouble();

    return new Point2D.Double(avgX, avgY);

//    Double x2 = 0.0, y2 = 0.0;
//    int count = 0;
//    for (Integer suid : suids) {
//      CyNode node = network.getNode(suid.longValue());
//      CyEdge edge = network.getEdge(suid.longValue());
//
//      if (node != null) {
//        View<CyNode> nodeView = this.view.getNodeView(node);
//        x2 += nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
//        y2 += nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
//        count++;
//      } else if (edge != null) {
//        View<CyNode> sourceView = this.view.getNodeView(edge.getSource());
//        View<CyNode> targetView = this.view.getNodeView(edge.getTarget());
//        x2 = x2 + sourceView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION)
//            + targetView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
//        y2 = y2 + sourceView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)
//            + targetView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
//        count += 2;
//      }
//    }
//
//    x2 = x2 / count;
//    y2 = y2 / count;
//
//    if (!avgX.equals(x2) || !avgY.equals(y2)) {
//      System.out.println("Discrepancy in averages");
//      System.out.print(avgX + " vs ");
//      System.out.println(x2);
//      System.out.print(avgY + " vs ");
//      System.out.println(y2);
//    } else {
//      System.out.println("Averages match");
//    }
//    return new Point2D.Double(x2, y2);
  }
}
