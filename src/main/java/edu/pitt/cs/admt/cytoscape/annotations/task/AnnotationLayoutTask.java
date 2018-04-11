package edu.pitt.cs.admt.cytoscape.annotations.task;

import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegate;
import java.awt.geom.Point2D;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
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
  };

  private final AnnotationManager annotationManager;
  private final CyNetworkView view;
  private final LayoutStrategy strategy;

  private AnnotationLayoutTask(final AnnotationManager annotationManager, final CyNetworkView view, final LayoutStrategy strategy) {
    this.annotationManager = annotationManager;
    this.view = view;
    this.strategy = strategy;
  }

  public static AnnotationLayoutTask CreateAnnotationLayoutTask(final AnnotationManager annotationManager, final CyNetworkView view) {
    return CreateAnnotationLayoutTask(annotationManager, view, LayoutStrategy.AVERAGE);
  }

  public static AnnotationLayoutTask CreateAnnotationLayoutTask(final AnnotationManager annotationManager, final CyNetworkView view, final LayoutStrategy strategy) {
    return new AnnotationLayoutTask(annotationManager, view, strategy);
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

  private void averagePositionLayout(final TaskMonitor monitor) {
    List<Annotation> annotations = this.annotationManager.getAnnotations(this.view);
    for (int i = 0; i < annotations.size(); i++) {
      Annotation annotation = annotations.get(i);
      UUID cyId = annotation.getUUID();
      try {
        Collection<Integer> components = StorageDelegate.getNetworkComponentsOnCytoscapeAnnotUUID(this.view.getModel().getSUID(), cyId);
        if (components.size() > 0) {
          Point2D location = averagePosition(components);
          annotation.moveAnnotation(location);
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
      monitor.setProgress((double) ((i+1) / annotations.size()));
    }
  }

  private Point2D averagePosition(Collection<Integer> suids) {
    CyNetwork network = this.view.getModel();
    Double x = 0.0, y = 0.0;
    int count = 0;

    for (Integer suid: suids) {
      CyNode node = network.getNode(suid.longValue());
      CyEdge edge = network.getEdge(suid.longValue());

      if (node != null) {
        View<CyNode> nodeView = this.view.getNodeView(node);
        x += nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
        y += nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
        count++;
      }

      if (edge != null) {
        View<CyNode> sourceView = this.view.getNodeView(edge.getSource());
        View<CyNode> targetView = this.view.getNodeView(edge.getTarget());
        x += sourceView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
        x += targetView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
        y += sourceView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
        y += targetView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
        count += 2;
      }

      x = x / count;
      y = y / count;
    }

    return new Point2D.Double(x, y);
  }
}
