package edu.pitt.cs.admt.cytoscape.annotations;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class CreateAnnotationTask extends AbstractTask {
    private final CyNetworkManager networkManager;
    private final CyNetworkView networkView;

    public CreateAnnotationTask(CyNetworkManager networkManager, CyNetworkView networkView) {
        this.networkManager = networkManager;
        this.networkView = networkView;
        System.out.println("Annotation task created");
    }

    public void run(TaskMonitor monitor) {
        System.out.println("Task ran successfully");
    }
}
