package edu.pitt.cs.admt.cytoscape.annotations.ui;

import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegate;
import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegateFactory;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class SearchAnnotationPanel extends JPanel implements Serializable {

  private static final long serialVersionUID = -7995662050240929535L;
  private final JLabel title = new JLabel("Search for CCD Annotations", SwingConstants.CENTER);
  private final JPanel namePanel = new JPanel();
  private final JLabel nameLabel = new JLabel("Name");
  private final JTextField nameField = new JTextField();
  private final JPanel filterPanel = new JPanel();
  private final JLabel filterLabel = new JLabel("Value");
  private final JTextField filterField = new JTextField();
  private final JButton searchButton = new JButton("Search");
  private final JButton clearButton = new JButton("Clear");
  private Long networkSUID = null;
//  private List<String> annotationNames = new LinkedList<>();
//  private JPanel resultContainer = new JPanel(new GridLayout(0, 1));
//  private JScrollPane resultPane = new JScrollPane(resultContainer, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  private JPanel resultPane = new JPanel();
  private Set<ResultItem> results = new LinkedHashSet<>();
  private final JComboBox<String> filterComparisonField = new JComboBox<>(
      new DefaultComboBoxModel<>(
          new Vector(Arrays.asList(new String[]{"", "equals", "not equals"}))
      )
  );

  public SearchAnnotationPanel() {
    // panel settings
    setBorder(new EmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(300, 800));
    setMaximumSize(new Dimension(400, 1000));

    // actions
    searchButton.addActionListener((ActionEvent e) -> {
      String name = nameField.getText().toLowerCase();
      Set<String> matches = Collections.EMPTY_SET;
      StorageDelegate delegate = getDelegate();
      if (delegate != null) {
        try {
          matches = delegate.getAllAnnotations()
              .stream()
              .map(Annotation::getName)
              .filter(a -> a.toLowerCase().contains(name))
              .collect(Collectors.toSet());
        } catch (SQLException exc) {
          exc.printStackTrace();
        }
        results.clear();
        resultPane.removeAll();
        Predicate<String> filterPredicate;
        String compare = filterField.getText();
        switch(filterComparisonField.getSelectedIndex()) {
          case 1:
            filterPredicate = (value) -> value.equals(compare);
            break;
          case 2:
            filterPredicate = (value) -> !value.equals(compare);
            break;
          case 0:
          default:
            filterPredicate = (value) -> true;
            break;
        }
        for (String m : matches) {
          Collection<AnnotToEntity> res;
          try {
            res = delegate.selectNodesWithAnnotation(m);
            res.stream()
                .map(AnnotToEntity::getValue)
                .map(a -> a.toString())
                .filter(filterPredicate)
                .forEach(a -> results.add(new ResultItem(m, a)));
            res = delegate.selectEdgesWithAnnotation(m);
            res.stream()
                .map(AnnotToEntity::getValue)
                .map(a -> a.toString())
                .filter(filterPredicate)
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
      filterField.setText("");
      filterComparisonField.setSelectedIndex(0);
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
    namePanel.setBorder(new EmptyBorder(2,2,2,2));
    nameField.setPreferredSize(new Dimension(180, 20));
    nameField.setHorizontalAlignment(JTextField.RIGHT);
    namePanel.add(nameLabel);
    namePanel.add(nameField);
    add(namePanel);
    add(new JLabel("Filter"));
    filterPanel.setBorder(new EmptyBorder(1, 2, 2, 2));
    filterComparisonField.setSize(70, filterComparisonField.getPreferredSize().height);
    filterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    filterPanel.add(filterLabel);
    filterPanel.add(filterComparisonField);
    filterField.setPreferredSize(new Dimension(80, 20));
    filterField.setHorizontalAlignment(JTextField.RIGHT);
    filterPanel.add(filterField);
    add(filterPanel);
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

//  public void refresh() {
//    Optional<StorageDelegate> storageDelegateOptional = StorageDelegateFactory.getDelegate(this.networkSUID);
//    if (storageDelegateOptional.isPresent()) {
//      StorageDelegate delegate = storageDelegateOptional.get();
//      try {
//        this.annotationNames = delegate.getAllAnnotations()
//            .stream()
//            .map(Annotation::getName)
//            .collect(Collectors.toList());
//      } catch (SQLException e) {
//        e.printStackTrace();
//      }
//    } else {
//      System.out.println("Search panel couldn't find storage delegate");
//    }
//  }

  public void refresh(Long suid) {
    this.networkSUID = suid;
//    refresh();
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
