package edu.pitt.cs.admt.cytoscape.annotations.ui;

import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegate;
import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegateFactory;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class SearchAnnotationPanel extends JPanel implements Serializable {

  private static final long serialVersionUID = -7995662050240929535L;
  private final JLabel title = new JLabel("Search for CCD Annotations", SwingConstants.CENTER);
  private final JLabel nameLabel = new JLabel("Name");
  private final JTextField nameField = new JTextField();
  private final JButton searchButton = new JButton("Search");
  private final JButton clearButton = new JButton("Clear");
  private Long networkSUID = null;
  private List<String> annotationNames = new LinkedList<>();
//  private JPanel resultContainer = new JPanel(new GridLayout(0, 1));
//  private JScrollPane resultPane = new JScrollPane(resultContainer, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  private JPanel resultPane = new JPanel();
  private Set<ResultItem> results = new LinkedHashSet<>();

  public SearchAnnotationPanel() {
    // panel settings
    setBorder(new EmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(300, 800));
    setMaximumSize(new Dimension(400, 1000));

    // actions
    searchButton.addActionListener((ActionEvent e) -> {
      String name = nameField.getText().toLowerCase();
      Set<String> matches = annotationNames.stream()
          .filter(a -> a.toLowerCase().contains(name))
          .collect(Collectors.toSet());
      StorageDelegate delegate = getDelegate();
      if (delegate != null) {
        results.clear();
        resultPane.removeAll();
        for (String m : matches) {
          Collection<AnnotToEntity> res;
          try {
            res = delegate.selectNodesWithAnnotation(m);
            res.stream()
                .map(AnnotToEntity::getValue)
                .forEach(a -> results.add(new ResultItem(m, a)));
            res = delegate.selectEdgesWithAnnotation(m);
            res.stream()
                .map(AnnotToEntity::getValue)
                .forEach(a -> results.add(new ResultItem(m, a)));
          } catch (Exception exc) {
            exc.printStackTrace();
          }
        }
        for (ResultItem r: results) {
          resultPane.add(r);
        }
      }
      resultPane.setPreferredSize(new Dimension(200, results.size() * 35));
      resultPane.setSize(new Dimension(200, results.size() * 35));
      revalidate();
    });

    clearButton.addActionListener((ActionEvent e) -> {
      nameField.setText("");
      for (ResultItem r: results) {
        resultPane.remove(r);
      }
      resultPane.removeAll();
      resultPane.setPreferredSize(new Dimension(200, 35));
      resultPane.setSize(new Dimension(200, 35));
      results.clear();
      revalidate();
    });

    add(title);
    nameField.setPreferredSize(new Dimension(200, 20));
    add(nameLabel);
    add(nameField);
    add(clearButton);
    add(searchButton);
//    resultPane.setViewportView(resultContainer);
    resultPane.setBackground(Color.WHITE);
    resultPane.setPreferredSize(new Dimension(200, 35));
    add(resultPane);
    setVisible(true);
  }

  private StorageDelegate getDelegate() {
    Optional<StorageDelegate> storageDelegateOptional = StorageDelegateFactory.getDelegate(this.networkSUID);
    if (storageDelegateOptional.isPresent()) {
      StorageDelegate delegate = storageDelegateOptional.get();
      return delegate;
    } else {
      return null;
    }
  }

  public void refresh() {
    Optional<StorageDelegate> storageDelegateOptional = StorageDelegateFactory.getDelegate(this.networkSUID);
    if (storageDelegateOptional.isPresent()) {
      StorageDelegate delegate = storageDelegateOptional.get();
      try {
        this.annotationNames = delegate.getAllAnnotations()
            .stream()
            .map(Annotation::getName)
            .collect(Collectors.toList());
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("Search panel couldn't find storage delegate");
    }
  }

  public void refresh(Long suid) {
    this.networkSUID = suid;
    refresh();
  }

  private class ResultItem extends JPanel {
    ResultItem(final String name, final Object value) {
      setBackground(Color.WHITE);
      setPreferredSize(new Dimension(200, 30));
      setMaximumSize(new Dimension(200, 30));
      setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
      add(new JLabel("Name: " + name));
      add(new JLabel("Value: " + value.toString()));
      setVisible(true);
    }
  }
}
