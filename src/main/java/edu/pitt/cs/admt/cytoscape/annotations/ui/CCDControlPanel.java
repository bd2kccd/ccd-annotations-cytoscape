package edu.pitt.cs.admt.cytoscape.annotations.ui;

import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import edu.pitt.cs.admt.cytoscape.annotations.task.CreateAnnotationTaskFactory;
import java.awt.Component;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.work.TaskManager;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class CCDControlPanel extends JPanel implements CytoPanelComponent, Serializable {

  private static final long serialVersionUID = 7128778486978079375L;
  private static final String NEW_ANNOTATION_NAME = "New...";

  private final CyApplicationManager applicationManager;
  private Long networkSUID = null;
  private Collection<Annotation> annotations = Collections.EMPTY_LIST;
  private Map<String, Annotation> annotationNameToObject = new HashMap<>();
  private JComboBox<String> annotationNames = new JComboBox<>();
  private Map<String, String> annotationDescriptions = new HashMap<>();
  private JLabel annotationsList;

  private final JLabel baseLabel = new JLabel("<html>You must select a network before managing CCD Annotations.</html>");
  private JTabbedPane basePanel = new JTabbedPane(JTabbedPane.BOTTOM);
  private CreateAnnotationPanel createPanel;
  private SearchAnnotationPanel searchPanel = new SearchAnnotationPanel();
  private JTabbedPane tabs = new JTabbedPane();

  public CCDControlPanel(
      final CyApplicationManager applicationManager,
      final TaskManager taskManager,
      final CreateAnnotationTaskFactory createAnnotationTaskFactory) {
    this.applicationManager = applicationManager;
    createPanel = new CreateAnnotationPanel(taskManager, createAnnotationTaskFactory);
    searchPanel = new SearchAnnotationPanel();
//    final Dimension minSize = new Dimension(250, 800);
//    final Dimension prefSize = new Dimension(300, 300);
    this.add(baseLabel);
    tabs.addTab("Create", null, createPanel, "Create new CCD Annotations");
    tabs.addTab("Search", null, searchPanel, "Search for CCD Annotations");
    this.setVisible(true);
  }

  private void updateView() {
    this.remove(baseLabel);
    this.remove(tabs);
    this.add(tabs);
  }


  /**
   * Update panel state to reflect change in selected network
   * @param suid Network SUID
   */
  public void refresh(Long suid) {
    this.networkSUID = suid;
    this.createPanel.refresh(suid);
    this.searchPanel.refresh(suid);
    this.updateView();
  }

  public Component getComponent() {
    return this;
  }

  public CytoPanelName getCytoPanelName() {
    return CytoPanelName.WEST;
  }

  public String getTitle() {
    return "CCD Annotations";
  }

  public Icon getIcon() {
    return null;
  }
}
