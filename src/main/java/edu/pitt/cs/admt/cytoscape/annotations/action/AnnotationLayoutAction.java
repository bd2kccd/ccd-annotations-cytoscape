package edu.pitt.cs.admt.cytoscape.annotations.action;

import java.awt.event.ActionEvent;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class AnnotationLayoutAction extends AbstractCyAction {

  public enum LayoutStrategy {
    AVERAGE
  };

  private final CySwingApplication application;
  private final DialogTaskManager taskManager;

  public AnnotationLayoutAction(CySwingApplication application, final DialogTaskManager taskManager) {
    super("Annotation Layout");
    this.application = application;
    this.taskManager = taskManager;
    this.setPreferredMenu("Layout");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    System.out.println("Action performed successfully");
  }

}
