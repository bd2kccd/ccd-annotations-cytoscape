package edu.pitt.cs.admt.cytoscape.annotations.network;

import static edu.pitt.cs.admt.cytoscape.annotations.task.AnnotationLayoutTask.CreateAnnotationLayoutTask;

import java.util.Collection;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.events.ViewChangedListener;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.TaskManager;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class ViewListener implements ViewChangedListener {

  private final CyApplicationManager applicationManager;
  private final AnnotationManager annotationManager;
  private final TaskManager taskManager;
  private boolean enabled = true;

  public ViewListener(
      final CyApplicationManager applicationManager,
      final AnnotationManager annotationManager,
      final TaskManager taskManager) {
    this.applicationManager = applicationManager;
    this.annotationManager = annotationManager;
    this.taskManager = taskManager;
  }

  public void enable() {
    relayout();
    this.enabled = true;
  }

  public void disable() {
    this.enabled = false;
  }

  public void toggle() {
    this.enabled = !this.enabled;
    if (enabled) {
      relayout();
    }
  }

  private void relayout() {
    taskManager.execute(CreateAnnotationLayoutTask(annotationManager, applicationManager.getCurrentNetworkView()).toTaskIterator());
  }

  @Override
  public void handleEvent(ViewChangedEvent<?> e) {
    if (!enabled) {
      return;
    }
    Collection<? extends ViewChangeRecord<?>> vps = e.getPayloadCollection();
    for (ViewChangeRecord v : vps) {
      String property = v.getVisualProperty().getIdString();
      Object model = v.getView().getModel();
      if ((model instanceof CyNode || model instanceof CyEdge) &&
          (property.equals("NODE_X_LOCATION") || property.equals("NODE_Y_LOCATION"))) {
        // annotations must be re-layout
        if (applicationManager.getCurrentNetworkView() != null) {
          relayout();
        }
        break;
      }
    }
  }
}