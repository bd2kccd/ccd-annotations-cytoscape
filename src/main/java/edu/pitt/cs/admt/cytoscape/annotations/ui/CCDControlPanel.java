package edu.pitt.cs.admt.cytoscape.annotations.ui;

import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegate;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import edu.pitt.cs.admt.cytoscape.annotations.task.CreateAnnotationTaskFactory;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serializable;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerListModel;
import javax.swing.SwingConstants;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.work.TaskManager;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class CCDControlPanel extends JPanel implements CytoPanelComponent, Serializable {

  private static final long serialVersionUID = 7128778486978079375L;

  private JLabel annotationsList;

  public CCDControlPanel(final TaskManager taskManager,
      final StorageDelegate storageDelegate,
      final CreateAnnotationTaskFactory createAnnotationTaskFactory) {

    // title
    JLabel label = new JLabel("New CCD Annotation\n", SwingConstants.CENTER);

    // extended attribute selection
    final String[] attributeOptions = {"Comment", "Posterior Probability"};
    final SpinnerListModel listModel = new SpinnerListModel(attributeOptions);
    final JSpinner attributeSpinner = new JSpinner(listModel);
    ((JSpinner.DefaultEditor) attributeSpinner.getEditor()).getTextField().setEditable(false);
    ((JSpinner.DefaultEditor) attributeSpinner.getEditor()).getTextField()
        .setPreferredSize(new Dimension(150, 20));

    // annotation data
    JTextArea annotationText = new JTextArea("CCD annotation text");
    annotationText.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(final FocusEvent e) {
        if (annotationText.getText().equals("CCD annotation text")) {
          annotationText.setText("");
        }
      }

      @Override
      public void focusLost(final FocusEvent e) {
        if (annotationText.getText().isEmpty()) {
          annotationText.setText("CCD annotation text");
        }
      }
    });
    annotationText.setPreferredSize(new Dimension(300, 100));
    annotationText.setLineWrap(true);
    annotationsList = new JLabel("");
    JButton button = new JButton("Create");

    button.addActionListener((ActionEvent e) -> {
      taskManager.execute(createAnnotationTaskFactory
          .createTaskIteratorAnnotationOnSelected(annotationText.getText()));
      annotationsList.setText("Added: " + annotationText.getText());
      annotationText.setText("CCD annotation text");
    });

    // search box
    JPanel searchPanel = new JPanel();
    JLabel searchLabel = new JLabel("\n\nSearch\n", SwingConstants.CENTER);
    JTextArea searchText = new JTextArea("Search");
    searchText.setPreferredSize(new Dimension(300, 100));
    searchText.setLineWrap(true);
    // TODO: Look into JTextArea property change methods (https://stackoverflow.com/questions/6478577/how-to-make-a-text-field-for-searchingwith-tips-like-a-google-search)
    searchText.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(final FocusEvent e) {
        if (searchText.getText().equals("Search")) {
          searchText.setText("");
        }
      }

      @Override
      public void focusLost(final FocusEvent e) {
        if (searchText.getText().isEmpty()) {
          searchText.setText("Search");
        }
      }
    });

    JButton searchButton = new JButton("Search");
    JButton clearButton = new JButton("Clear");

//    searchButton.addActionListener(new SearchActionListener(storageDelegate, searchText.getText()));

    clearButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.out.println("Clearing...");
      }
    });

    // Java FX Experiment
    JScrollPane scrollPane = new JScrollPane(annotationText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setVisible(true);
    this.add(label);
    this.add(new JTextArea("\n\n"));    // line break
    this.add(attributeSpinner);
//        this.add(annotationText);
    this.add(scrollPane);
    this.add(new JTextArea("\n"));      // line break
    this.add(button);
    this.add(new JLabel("\n"));    // line break
    this.add(annotationsList);
    this.add(new JLabel("\n\n"));    // line break
    this.add(searchLabel);
    this.add(searchText);
    this.add(searchButton);
    this.add(clearButton);
    this.setVisible(true);
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
