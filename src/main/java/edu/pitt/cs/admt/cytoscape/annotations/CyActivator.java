package edu.pitt.cs.admt.cytoscape.annotations;

import edu.pitt.cs.admt.cytoscape.annotations.network.NetworkListener;
import edu.pitt.cs.admt.cytoscape.annotations.task.CreateAnnotationTaskFactory;
import edu.pitt.cs.admt.cytoscape.annotations.ui.CCDControlPanel;
import java.util.Properties;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.BundleContext;

/**
 * {@code Activator} is the starting point for OSGi bundles <p> When OSGi starts the bundle, it will
 * invoke {@Activator}'s {@code start} method
 */
public class CyActivator extends AbstractCyActivator {

  public CyActivator() {
    super();
  }

  /**
   * Sets up app
   */
  @Override
  public void start(BundleContext context) {
    CyApplicationManager applicationManager = getService(context, CyApplicationManager.class);
    TaskManager taskManager = getService(context, TaskManager.class);
    AnnotationManager annotationManager = getService(context, AnnotationManager.class);
    AnnotationFactory<TextAnnotation> textAnnotationFactory = getService(context,
        AnnotationFactory.class, "(type=TextAnnotation.class)");

    // CCD Annotation creation service
    CreateAnnotationTaskFactory createAnnotationTaskFactory = new CreateAnnotationTaskFactory(
        applicationManager, annotationManager, textAnnotationFactory);
    registerService(context, createAnnotationTaskFactory, TaskFactory.class, new Properties());

    // listeners
    NetworkListener networkListener = new NetworkListener(annotationManager, textAnnotationFactory, taskManager);
    registerService(context, networkListener, NetworkViewAddedListener.class, new Properties());

    // ui components
    CCDControlPanel ccdControlPanel = new CCDControlPanel(taskManager, createAnnotationTaskFactory);
    registerService(context, ccdControlPanel, CytoPanelComponent.class, new Properties());
//        ControlPanelAction controlPanelAction = new ControlPanelAction(application, ccdControlPanel);
//        registerService(context, controlPanelAction, CyAction.class, new Properties());
  }
}
