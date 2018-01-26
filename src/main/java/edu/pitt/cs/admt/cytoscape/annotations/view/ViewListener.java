package edu.pitt.cs.admt.cytoscape.annotations.view;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.events.ViewChangedListener;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class ViewListener implements ViewChangedListener {

  public void handleEvent(final ViewChangedEvent e) {
    System.out.println(((CyNetworkView)e.getSource()).getSUID());
  }
}
