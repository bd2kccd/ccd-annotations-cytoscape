package edu.pitt.cs.admt.cytoscape.annotations.network;

//import static edu.pitt.cs.admt.cytoscape.annotations.CyActivator.AutoLayoutProp;
//import static edu.pitt.cs.admt.cytoscape.annotations.CyActivator.ccdAnnotationProperties;
import static edu.pitt.cs.admt.cytoscape.annotations.task.AnnotationLayoutTask.CreateAnnotationLayoutTask;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNode;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.events.ViewChangedListener;
import org.cytoscape.view.presentation.annotations.AnnotationManager;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class ViewListener implements ViewChangedListener {

  private final CyApplicationManager applicationManager;
  private final AnnotationManager annotationManager;
//  private final CyProperty<Properties> layoutProperty;
  private Boolean enabled = true;

  public ViewListener(final CyApplicationManager applicationManager, final AnnotationManager annotationManager) {
    this.applicationManager = applicationManager;
    this.annotationManager = annotationManager;
//    this.layoutProperty = layoutProperty;
  }

  public void enable() {
    this.enabled = true;
//    setSessionProperty();
    relayout();
  }

  public void disable() {
    this.enabled = false;
//    setSessionProperty();
  }

  public void toggle() {
    if (this.enabled) {
      disable();
    } else {
      enable();
    }
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  private void relayout() {
    if (applicationManager.getCurrentNetworkView() != null) {
      CreateAnnotationLayoutTask(
          annotationManager, applicationManager.getCurrentNetworkView()).run();
    }
  }

  private void relayout(Collection<Integer> nodes, Collection<Integer> edges) {
    if (applicationManager.getCurrentNetworkView() != null) {
      CreateAnnotationLayoutTask(
          annotationManager, applicationManager.getCurrentNetworkView(), nodes, edges).run();
    }
  }

  @Override
  public void handleEvent(ViewChangedEvent<?> e) {
    if (!enabled || applicationManager.getCurrentNetwork() == null) {
      return;
    }
    // get set of nodes and edges affected
    Set<Integer> nodes = new HashSet<>();
    Set<Integer> edges = new HashSet<>();
    for (ViewChangeRecord v : e.getPayloadCollection()) {
      String property = v.getVisualProperty().getIdString();
      if (property.equals("NODE_X_LOCATION") || property.equals("NODE_Y_LOCATION")) {
        // add to set of affected nodes/edges
        CyNode node = (CyNode) v.getView().getModel();
        nodes.add(node.getSUID().intValue());
        edges.addAll(applicationManager.getCurrentNetwork()
            .getAdjacentEdgeList(node, Type.ANY)
            .stream()
            .map(CyEdge::getSUID)
            .map(Long::intValue)
            .collect(Collectors.toSet()));
      }
    }
    relayout(nodes, edges);
  }

//  private void setSessionProperty() {
//    layoutProperty.getProperties().setProperty(AutoLayoutProp, this.enabled.toString());
//    layoutProperty.getProperties().put(AutoLayoutProp, this.enabled.toString());
//    ccdAnnotationProperties.setProperty(AutoLayoutProp, this.enabled.toString());
//  }
}