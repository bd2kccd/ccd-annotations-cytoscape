package edu.pitt.cs.admt.cytoscape.annotations.task;

import java.awt.event.ActionEvent;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class CreateAnnotationAction extends AbstractCyAction {

  private static Integer counter = 0;

  private final CySwingApplication application;
  private final CytoPanel cytoPanel;
  private final DialogTaskManager dialogTaskManager;
  private final CreateAnnotationTaskFactory createAnnotationTaskFactory;

  public String annotationText;
  public Integer xCoord;
  public Integer yCoord;

  public CreateAnnotationAction(CySwingApplication application,
      final DialogTaskManager dialogTaskManager,
      final CreateAnnotationTaskFactory createAnnotationTaskFactory) {
    super("CCDAnnotation");
    setPreferredMenu("File.New");
    this.application = application;
    this.dialogTaskManager = dialogTaskManager;
    this.cytoPanel = this.application.getCytoPanel(CytoPanelName.WEST);
    this.createAnnotationTaskFactory = createAnnotationTaskFactory;
//        this.createAnnotationFactory = createAnnotationFactory;
  }

  public void actionPerformed(ActionEvent e) {
//        SpinnerNumberModel numberModel = new SpinnerNumberModel(
//                new Integer(20),    // value
//                new Integer(0),     // min
//                new Integer(100),   // max
//                new Integer(1)      // step
//        );
//        JSpinner numberChooser = new JSpinner(numberModel);
//        JOptionPane.showMessageDialog(null, numberChooser);
    System.out.println("Action performed successfully");
    dialogTaskManager.execute(createAnnotationTaskFactory.createTaskIterator());
  }
}
