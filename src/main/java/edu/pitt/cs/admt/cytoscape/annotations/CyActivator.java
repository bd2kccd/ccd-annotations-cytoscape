package edu.pitt.cs.admt.cytoscape.annotations;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

/**
 * {@code Activator} is the starting point for OSGi bundles
 *
 * When OSGi starts the bundle, it will invoke {@Activator}'s
 * {@code start} method
 *
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
        CyNetworkManager networkManager = getService(context, CyNetworkManager.class);
        CyApplicationManager applicationManager = getService(context, CyApplicationManager.class);
        CyNetworkNaming networkNaming = getService(context, CyNetworkNaming.class);
        CyNetworkFactory networkFactory = getService(context, CyNetworkFactory.class);
        CyNetworkView networkView = getService(context, CyNetworkView.class);
        CyNetworkViewManager networkViewManager = getService(context, CyNetworkViewManager.class);
        CySwingApplication application = getService(context, CySwingApplication.class);
        DialogTaskManager dialogTaskManager = getService(context, DialogTaskManager.class);
        CyNetworkViewFactory networkViewFactory = getService(context, CyNetworkViewFactory.class);
        LoadVizmapFileTaskFactory loadVizmapFileTaskFactory = getService(context, LoadVizmapFileTaskFactory.class);
        VisualMappingManager visualMappingManager = getService(context, VisualMappingManager.class);
        AnnotationManager annotationManager = getService(context, AnnotationManager.class);
        AnnotationFactory<TextAnnotation> textAnnotationFactory = getService(context, AnnotationFactory.class, "(type=TextAnnotation.class)");

        CreateAnnotationTaskFactory createAnnotationTaskFactory = new CreateAnnotationTaskFactory(networkManager, networkView);
        registerService(context, createAnnotationTaskFactory, TaskFactory.class, new Properties());

        CreateAnnotationAction createAnnotationAction = new CreateAnnotationAction(application, dialogTaskManager, createAnnotationTaskFactory);
        registerService(context, createAnnotationAction, CyAction.class, new Properties());

        // listeners
        NetworkListener networkListener = new NetworkListener();
        registerService(context, networkListener, NetworkAddedListener.class, new Properties());

        // Trying to add to control panel
        CCDControlPanel ccdControlPanel = new CCDControlPanel(applicationManager, networkViewManager, annotationManager, textAnnotationFactory);
        registerService(context, ccdControlPanel, CytoPanelComponent.class, new Properties());
        ControlPanelAction controlPanelAction = new ControlPanelAction(application, ccdControlPanel);
        registerService(context, controlPanelAction, CyAction.class, new Properties());
    }
}
