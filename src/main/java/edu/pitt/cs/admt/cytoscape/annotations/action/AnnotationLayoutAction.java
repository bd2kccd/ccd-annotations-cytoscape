package edu.pitt.cs.admt.cytoscape.annotations.action;

import edu.pitt.cs.admt.cytoscape.annotations.network.ViewListener;
import java.awt.event.ActionEvent;
import org.cytoscape.application.swing.AbstractCyAction;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class AnnotationLayoutAction extends AbstractCyAction {

  private static final long serialVersionUID = 4125398496302851531L;

  private final ViewListener listener;
  private static boolean automaticLayout = true;
  private static final String title = "Automatic CCD Annotation Relayout";

  public AnnotationLayoutAction(final ViewListener listener) {
    super(title + " ✓");
    this.setPreferredMenu("Layout");

    this.listener = listener;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    listener.toggle();
    automaticLayout = !automaticLayout;
    this.setName(automaticLayout ? title + " ✓" : title);
  }
}
