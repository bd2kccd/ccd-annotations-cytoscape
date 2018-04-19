package edu.pitt.cs.admt.cytoscape.annotations;

import edu.pitt.cs.admt.cytoscape.annotations.action.AutomaticAnnotationLayoutAction;
import edu.pitt.cs.admt.cytoscape.annotations.action.ManualAnnotationLayoutAction;
import edu.pitt.cs.admt.cytoscape.annotations.network.NetworkListener;
import edu.pitt.cs.admt.cytoscape.annotations.network.ViewListener;
import edu.pitt.cs.admt.cytoscape.annotations.session.SessionListener;
import edu.pitt.cs.admt.cytoscape.annotations.task.ComponentHighlightTaskFactory;
import edu.pitt.cs.admt.cytoscape.annotations.task.CreateAnnotationTaskFactory;
import edu.pitt.cs.admt.cytoscape.annotations.ui.CCDControlPanel;
import java.util.Properties;
import java.util.Set;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.ViewChangedListener;
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

//  public static final String AutoLayoutProp = "CCD_AUTOMATIC_ANNOTATION_LAYOUT";
//  public static Properties ccdAnnotationProperties = new Properties();

  /**
   * Sets up app
   */
  @Override
  public void start(BundleContext context) {
    CyApplicationManager applicationManager = getService(context, CyApplicationManager.class);
//    CySessionManager sessionManager = getService(context, CySessionManager.class);
    TaskManager taskManager = getService(context, TaskManager.class);
    AnnotationManager annotationManager = getService(context, AnnotationManager.class);
    AnnotationFactory<TextAnnotation> textAnnotationFactory = getService(context, AnnotationFactory.class, "(type=TextAnnotation.class)");

    // properties
//    CyProperty<Properties> automaticLayoutProperty = null;
//    CySession session = sessionManager.getCurrentSession();
//    if (session.equals(null)) {
//      System.out.println("Session is null");
//    } else {
//      Set<CyProperty<?>> properties = session.getProperties();
//      if (properties.equals(null)) {
//        System.out.println("Properties is null");
//      } else {
//        CyProperty<?> prop = null;
//        for (CyProperty<?> property : properties) {
//          if (property.getName() != null && property.getName().equals(AutoLayoutProp)) {
//            prop = property;
//            break;
//          }
//        }
//        if (prop != null) {
//          automaticLayoutProperty = (CyProperty<Properties>) prop;
//          ccdAnnotationProperties = automaticLayoutProperty.getProperties();
//          System.out.println("Added property: " + ccdAnnotationProperties.getProperty(AutoLayoutProp));
//        } else {
//          ccdAnnotationProperties.setProperty(AutoLayoutProp, Boolean.TRUE.toString());
//          automaticLayoutProperty = new SimpleCyProperty(AutoLayoutProp, ccdAnnotationProperties, Boolean.TYPE, SavePolicy.SESSION_FILE_AND_CONFIG_DIR);
//          System.out.println("Property was: " + ccdAnnotationProperties.getProperty(AutoLayoutProp));
//        }
//      }
//    }
//    registerService(context, automaticLayoutProperty, CyProperty.class, new Properties());

    // tasks
    CreateAnnotationTaskFactory createAnnotationTaskFactory = new CreateAnnotationTaskFactory(
        applicationManager, annotationManager, textAnnotationFactory, taskManager);
    registerService(context, createAnnotationTaskFactory, TaskFactory.class, new Properties());

    ComponentHighlightTaskFactory highlightTaskFactory = new ComponentHighlightTaskFactory(applicationManager);
    registerService(context, highlightTaskFactory, TaskFactory.class, new Properties());

    // ui components
    CCDControlPanel ccdControlPanel = new CCDControlPanel(applicationManager, taskManager, createAnnotationTaskFactory, highlightTaskFactory);
    registerService(context, ccdControlPanel, CytoPanelComponent.class, new Properties());

    // listeners
    ViewListener viewListener = new ViewListener(applicationManager, annotationManager);
    registerService(context, viewListener, ViewChangedListener.class, new Properties());

    NetworkListener networkListener = new NetworkListener(annotationManager, textAnnotationFactory, taskManager, ccdControlPanel);
    registerService(context, networkListener, NetworkViewAddedListener.class, new Properties());
    registerService(context, networkListener, SetCurrentNetworkListener.class, new Properties());

//    SessionListener sessionListener = new SessionListener();
//    registerService(context, sessionListener, SessionLoadedListener.class, new Properties());

    // actions
    ManualAnnotationLayoutAction annotationLayoutAction = new ManualAnnotationLayoutAction(applicationManager, annotationManager, taskManager);
    registerService(context, annotationLayoutAction, CyAction.class, new Properties());

    AutomaticAnnotationLayoutAction autoAnnotationLayoutAction = new AutomaticAnnotationLayoutAction(viewListener);
    registerService(context, autoAnnotationLayoutAction, CyAction.class, new Properties());
  }
}
