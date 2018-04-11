package edu.pitt.cs.admt.cytoscape.annotations.task;

import java.util.Collection;
import java.util.Collections;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class ComponentHighlightTaskFactory extends AbstractTaskFactory {

  private final CyApplicationManager applicationManager;

  public ComponentHighlightTaskFactory(final CyApplicationManager applicationManager) {
    this.applicationManager = applicationManager;
  }

  public ComponentHighlightTask clearComponentHighlight() {
    return ComponentHighlightTask.CreateComponentHighlightTask(this.applicationManager.getCurrentNetwork(),
        Collections.emptyList());
  }

  public ComponentHighlightTask createComponentHighlightTask(final Collection<Integer> suids) {
    return ComponentHighlightTask.CreateComponentHighlightTask(this.applicationManager.getCurrentNetwork(), suids);
  }

  @Override
  /**
   * Do not use this method. Overriding it is required to extend AbstractTaskFactory.
   * @see org.cytoscape.work.AbstractTaskFactory;
   */
  public TaskIterator createTaskIterator() {
    return new TaskIterator();
  }

}
