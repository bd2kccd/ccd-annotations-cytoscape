package edu.pitt.cs.admt.cytoscape.annotations.ui;

import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegate;
import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegateFactory;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import edu.pitt.cs.admt.cytoscape.annotations.task.CreateAnnotationTask;
import edu.pitt.cs.admt.cytoscape.annotations.task.CreateAnnotationTaskFactory;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerListModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
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
  private JPanel searchPanel = new JPanel();

  public CCDControlPanel(
      final CyApplicationManager applicationManager,
      final TaskManager taskManager,
      final CreateAnnotationTaskFactory createAnnotationTaskFactory) {
    this.applicationManager = applicationManager;
    createPanel = new CreateAnnotationPanel(taskManager, createAnnotationTaskFactory);
//    final Dimension minSize = new Dimension(250, 800);
//    final Dimension prefSize = new Dimension(300, 300);
//    this.basePanel.setMinimumSize(minSize);
//    this.basePanel.setPreferredSize(prefSize);
//    this.createPanel.setMaximumSize(minSize);
//    this.createPanel.setPreferredSize(prefSize);
//    this.searchPanel.setMaximumSize(minSize);
//    this.searchPanel.setPreferredSize(prefSize);
//    this.add(baseLabel);
    this.add(baseLabel);
    this.setVisible(true);
  }

  private void updateView() {
    this.remove(baseLabel);
    this.add(createPanel);
//    this.remove(baseLabel);
//    this.remove(basePanel);
//    this.basePanel.removeAll();
//    this.createPanel.removeAll();
//    this.searchPanel.removeAll();
//    if (this.networkSUID == null) {
//      this.add(baseLabel);
//    } else {
//      this.createPanel.add(new JLabel("Create"));
//      this.searchPanel.add(new JLabel("Search"));
//      this.basePanel.addTab("Create", null, this.createPanel, "Create CCD Annotations");
//      this.basePanel.addTab("Search", null, this.searchPanel, "Search CCD Annotations");
//      this.add(basePanel, BorderLayout.CENTER);
//    }
  }

//  public CCDControlPanel(
//      final CyApplicationManager applicationManager,
//      final TaskManager taskManager,
//      final CreateAnnotationTaskFactory createAnnotationTaskFactory) {
//    this.applicationManager = applicationManager;
//
////    JPanel createPanel = new JPanel();
////    JPanel searchPanel = new JPanel();
//
//    JPanel creationPanel = new JPanel();
//    creationPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
//    JLabel nameLabel = new JLabel("Annotation name");
//
//    JPanel descriptionPanel = new JPanel();
//    descriptionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
//    JLabel descriptionLabel = new JLabel("Annotation description");
//
//    JPanel valuePanel = new JPanel();
//    valuePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
//    JLabel valueLabel = new JLabel("Annotation value");
//
//    // title
//    JLabel label = new JLabel("New CCD Annotation\n", SwingConstants.CENTER);
//
//    // extended attribute selection
//    final String[] attributeOptions = {"<New>", "Comment", "Posterior Probability"};
//    final SpinnerListModel listModel = new SpinnerListModel(attributeOptions);
//    final JSpinner attributeSpinner = new JSpinner(listModel);
//    ((JSpinner.DefaultEditor) attributeSpinner.getEditor()).getTextField().setEditable(false);
//    ((JSpinner.DefaultEditor) attributeSpinner.getEditor()).getTextField()
//        .setPreferredSize(new Dimension(150, 20));
//    annotationNames.addActionListener((ActionEvent e) -> {
//      String selected = (String) annotationNames.getSelectedItem();
//      String description = "Annotation description: ";
//      if (!selected.equals(NEW_ANNOTATION_NAME)) {
//        description += annotationNameToObject.get(selected).getDescription();
//      }
//      descriptionLabel.setText(description);
//    });
////    final JComboBox attributeCombo = new JComboBox(attributeOptions);
////    attributeCombo.addActionListener((ActionEvent e) -> {
////      Long networkSUID = this.applicationManager.getCurrentNetwork().getSUID();
////      Optional<StorageDelegate> storageDelegateOptional = StorageDelegateFactory.getDelegate(networkSUID);
////      if (storageDelegateOptional.isPresent()) {
////        StorageDelegate storageDelegate = storageDelegateOptional.get();
////        Collection<Annotation> annotations;
////        try {
////          annotations = storageDelegate.getAllAnnotations();
////          for (Annotation a: annotations) {
////            System.out.println(a.getDescription());
////          }
////        } catch (Exception exc) {
////          exc.printStackTrace();
////        }
////      }
////      String selected = (String)((JComboBox)e.getSource()).getSelectedItem();
////      System.out.println("Selected: " + selected);
////    });
//
//    // annotation data
//    JTextArea annotationText = new JTextArea("CCD annotation text");
//    annotationText.addFocusListener(new FocusListener() {
//      @Override
//      public void focusGained(final FocusEvent e) {
//        if (annotationText.getText().equals("CCD annotation text")) {
//          annotationText.setText("");
//        }
//      }
//
//      @Override
//      public void focusLost(final FocusEvent e) {
//        if (annotationText.getText().isEmpty()) {
//          annotationText.setText("CCD annotation text");
//        }
//      }
//    });
//
//    annotationText.setPreferredSize(new Dimension(300, 100));
//    annotationText.setLineWrap(true);
//    annotationsList = new JLabel("");
//    JButton button = new JButton("Create");
//
//    button.addActionListener((ActionEvent e) -> {
//      taskManager.execute(
//          createAnnotationTaskFactory
//              .createOnSelected(annotationText.getText())
//              .createTaskIterator());
//      annotationsList.setText("Added: " + annotationText.getText());
//      annotationText.setText("CCD annotation text");
//    });
//
//    // search box
//    JPanel searchPanel = new JPanel();
//    JLabel searchLabel = new JLabel("\n\nSearch\n", SwingConstants.CENTER);
//    JTextArea searchText = new JTextArea("Search");
//    searchText.setPreferredSize(new Dimension(300, 100));
//    searchText.setLineWrap(true);
//    // TODO: Look into JTextArea property change methods (https://stackoverflow.com/questions/6478577/how-to-make-a-text-field-for-searchingwith-tips-like-a-google-search)
//    searchText.addFocusListener(new FocusListener() {
//      @Override
//      public void focusGained(final FocusEvent e) {
//        if (searchText.getText().equals("Search")) {
//          searchText.setText("");
//        }
//      }
//
//      @Override
//      public void focusLost(final FocusEvent e) {
//        if (searchText.getText().isEmpty()) {
//          searchText.setText("Search");
//        }
//      }
//    });
//
//    JButton searchButton = new JButton("Search");
//    JButton clearButton = new JButton("Clear");
//
////    searchButton.addActionListener(new SearchActionListener(storageDelegate, searchText.getText()));
//
//    clearButton.addActionListener(new ActionListener() {
//      @Override
//      public void actionPerformed(ActionEvent e) {
//        System.out.println("Clearing...");
//      }
//    });
//
//    // Java FX Experiment
//    JScrollPane scrollPane = new JScrollPane(annotationText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
//        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//    scrollPane.setVisible(true);
//    this.add(label);
//    this.add(new JTextArea("\n\n"));    // line break
//    this.add(attributeSpinner);
////        this.add(annotationText);
//    creationPanel.add(nameLabel);
//    creationPanel.add(annotationNames);
//    descriptionPanel.add(descriptionLabel);
//    valuePanel.add(valueLabel);
//    this.add(creationPanel);
//    this.add(descriptionPanel);
//    this.add(valuePanel);
//    this.add(scrollPane);
//    this.add(new JTextArea("\n"));      // line break
//    this.add(button);
//    this.add(new JLabel("\n"));    // line break
//    this.add(annotationsList);
//    this.add(new JLabel("\n\n"));    // line break
//    this.add(searchLabel);
//    this.add(searchText);
//    this.add(searchButton);
//    this.add(clearButton);
//    this.setVisible(true);
//  }

  /**
   * Update panel state to reflect change in selected network
   * @param suid Network SUID
   */
  public void refresh(Long suid) throws Exception {
    this.networkSUID = suid;
    this.createPanel.refresh(suid);
    this.updateView();
    Optional<StorageDelegate> storageDelegateOptional = StorageDelegateFactory.getDelegate(this.networkSUID);
    if (storageDelegateOptional.isPresent()) {
//      StorageDelegate storageDelegate = storageDelegateOptional.get();
//      try {
//        // update annotation collection
//        this.annotations = storageDelegate.getAllAnnotations();
//        System.out.println("Fetched " + this.annotations.size() + " annotations");
//        // update mapping to annotation objects
//        for (Annotation a: this.annotations) {
//          this.annotationNameToObject.put(a.getName(), a);
//        }
//        // update annotation name combo box
//        Vector<String> names = new Vector<>(this.annotations
//            .stream()
//            .map(Annotation::getName)
//            .collect(Collectors.toList()));
//        names.insertElementAt(NEW_ANNOTATION_NAME, 0);
//        this.annotationNames.setModel(new DefaultComboBoxModel<>(names));
//        this.updateView();
//      } catch (SQLException e) {
//        e.printStackTrace();
//      }
    } else {
      System.out.println("\nMissing network???\n");
    }
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

//  public class SearchActionListener implements ActionListener {
//
//    private StorageDelegate storageDelegate;
//    private String searchString;
//
//    public SearchActionListener(final StorageDelegate storageDelegate, final String searchString) {
//      super();
//      System.out.println("Creating action listener");
//      System.out.println("search string: " + searchString);
//      this.storageDelegate = new StorageDelegate();
//      this.searchString = searchString;
//    }
//
//    public void actionPerformed(ActionEvent e) {
//      System.out.println("Running action performed");
//      try {
//        Collection<AnnotToEntity> result = this.storageDelegate
//            .searchAnnotations(this.searchString);
//        for (AnnotToEntity entity : result) {
//          System.out.println(entity.getValue());
//        }
//        this.storageDelegate.close();
//      } catch (Exception exc) {
//        exc.printStackTrace();
//      }
//    }
//  }
}
