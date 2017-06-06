package edu.pitt.cs.admt.cytoscape.annotations;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class CreateAnnotationTaskFactory extends AbstractTaskFactory {
    private final CyNetworkManager networkManager;
    private final CyNetworkView networkView;

    public CreateAnnotationTaskFactory(final CyNetworkManager networkManager, final CyNetworkView networkView) {
        this.networkManager = networkManager;
        this.networkView = networkView;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new CreateAnnotationTask(networkManager, networkView));
    }
}
