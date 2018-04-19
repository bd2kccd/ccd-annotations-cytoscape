package edu.pitt.cs.admt.cytoscape.annotations.session;

import java.util.Set;
import org.cytoscape.property.CyProperty;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class SessionListener implements SessionLoadedListener {

  @Override
  public void handleEvent(SessionLoadedEvent event) {
    CySession session = event.getLoadedSession();
    Set<CyProperty<?>> properties = session.getProperties();
    System.out.println("Found properties");
    properties.forEach(s -> System.out.println(s.getProperties()));
  }
}
