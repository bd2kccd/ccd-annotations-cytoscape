package edu.pitt.cs.admt.cytoscape.annotations.ui;

import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegate;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Predicate;
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
  private final JComboBox<String> filterComparisonField = new JComboBox<>(
      new DefaultComboBoxModel<>(
          new Vector(Arrays.asList(new String[]{"", "equals", "not equals", ">", "≥", "<", "≤"}))
      )
  );
  private Long networkSUID = null;
  //  private List<String> annotationNames = new LinkedList<>();
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
//      Set<String> matches = Collections.EMPTY_SET;
//      try {
//        matches = StorageDelegate.getAllAnnotations(this.networkSUID)
//            .stream()
//            .map(Annotation::getName)
//            .filter(a -> a.toLowerCase().contains(name))
//            .collect(Collectors.toSet());
//      } catch (SQLException exc) {
//        exc.printStackTrace();
//      }
      results.clear();
      resultPane.removeAll();
      Predicate<String> filterPredicate;
      Function<Object, Boolean> filterFunc;
      Object compare = null;
      try {
        compare = Integer.parseInt(filterField.getText());
      } catch (Exception ex) { }
      try {
        compare = Float.parseFloat(filterField.getText());
      } catch (Exception ex) { }
      if (compare == null && (filterField.getText().equalsIgnoreCase("true") || filterField.getText().equalsIgnoreCase("false"))) {
        compare = Boolean.parseBoolean(filterField.getText());
      }
      if (compare == null && filterField.getText().length() == 1) {
        compare = filterField.getText().charAt(0);
      }
      if (compare == null) {
        compare = filterField.getText();
      }
      final Object comparer = compare;

      switch (filterComparisonField.getSelectedIndex()) {
        case 1:
          if (comparer instanceof String) {
            filterFunc = (value) -> value.toString().equals((String) comparer);
          } else {
            filterFunc = (value) -> value == comparer;
          }
          break;
        case 2:
          if (comparer instanceof String) {
            filterFunc = (value) -> !value.toString().equals((String) comparer);
          } else {
            filterFunc = (value) -> value != comparer;
          }
          break;
        case 3:
          if (comparer instanceof String) {
            filterFunc = (value) -> value.toString().compareTo((String) comparer) > 0;
          } else if (comparer instanceof Boolean){
            filterFunc = (value) -> false;
          } else if (comparer instanceof Float || comparer instanceof Integer){
            filterFunc = (value) -> (value instanceof Float || value instanceof Integer) && (Float) value > (Float) comparer;
          } else {
            filterFunc = (value) -> false;
          }
          break;
        case 4:
          if (comparer instanceof String) {
            filterFunc = (value) -> value.toString().compareTo((String) comparer) >= 0;
          } else if (comparer instanceof Boolean){
            filterFunc = (value) -> false;
          } else if (comparer instanceof Float || comparer instanceof Integer){
            filterFunc = (value) -> (value instanceof Float || value instanceof Integer) && (Float) value >= (Float) comparer;
          } else {
            filterFunc = (value) -> false;
          }
          break;
        case 5:
          if (comparer instanceof String) {
            filterFunc = (value) -> value.toString().compareTo((String) comparer) < 0;
          } else if (comparer instanceof Boolean){
            filterFunc = (value) -> false;
          } else if (comparer instanceof Float || comparer instanceof Integer){
            filterFunc = (value) -> (value instanceof Float || value instanceof Integer) && (Float) value < (Float) comparer;
          } else {
            filterFunc = (value) -> false;
          }
          break;
        case 6:
          if (comparer instanceof String) {
            filterFunc = (value) -> value.toString().compareTo((String) comparer) <= 0;
          } else if (comparer instanceof Boolean){
            filterFunc = (value) -> false;
          } else if (comparer instanceof Float || comparer instanceof Integer){
            filterFunc = (value) -> (value instanceof Float || value instanceof Integer) && (Float) value <= (Float) comparer;
          } else {
            filterFunc = (value) -> false;
          }
          break;
        case 0:
        default:
          filterFunc = (value) -> true;
          break;
      }
      try {
        Collection<AnnotToEntity> res = StorageDelegate
            .searchEntitiesWithPredicate(this.networkSUID, name, filterFunc);
        HashMap<UUID, Annotation> annotationNameMap = new HashMap<>();
        for (AnnotToEntity r: res) {
          if (!annotationNameMap.containsKey(r.getAnnotationId())) {
            annotationNameMap.put(r.getAnnotationId(), StorageDelegate.getAnnotation(this.networkSUID, r.getAnnotationId()).get());
          }
          Annotation a = annotationNameMap.get(r.getAnnotationId());
          results.add(new ResultItem(a.getName(), a.getDescription(), r.getValue()));
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        return;
      }
//      StorageDelegate.searchEntitiesWithPredicate(this.networkSUID, name, filterFunc)
//          .stream()
//          .map(a -> results.add(new ResultItem(StorageDelegate.getAnnotation(this.networkSUID, a.)a.getValue())))
//      for (String m : matches) {
//        Collection<AnnotToEntity> res;
//        try {
//          res = StorageDelegate.selectNodesWithAnnotation(this.networkSUID, m);
//          res.stream()
//              .map(AnnotToEntity::getValue)
//              .map(a -> a.toString())
//              .filter(filterPredicate)
//              .forEach(a -> results.add(new ResultItem(m, a)));
//          res = StorageDelegate.selectEdgesWithAnnotation(this.networkSUID, m);
//          res.stream()
//              .map(AnnotToEntity::getValue)
//              .map(a -> a.toString())
//              .filter(filterPredicate)
//              .forEach(a -> results.add(new ResultItem(m, a)));
//        } catch (Exception exc) {
//          exc.printStackTrace();
//        }
//      }
      for (ResultItem r : results) {
        resultPane.add(r);
      }
      try {
        System.out.println("Testing storage delegate method:");
        Collection<AnnotToEntity> test = StorageDelegate.selectEntitiesWithAnnotationNameAndPredicate(this.networkSUID, name, filterFunc);
        System.out.println("Found: " + test.size());
        for (AnnotToEntity ae: test) {
          System.out.println(ae.getValue());
        }
        System.out.println("\nTesting search method:");
        Collection<AnnotToEntity> test2 = StorageDelegate.searchEntitiesWithPredicate(this.networkSUID, name, filterFunc);
        System.out.println("Found: " + test2.size());
        for (AnnotToEntity ae: test2) {
          System.out.println(ae.getValue());
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      System.out.println();
      int height = 0;
      for (ResultItem r: results) {
        height += r.getMaximumSize().height + 5;
      }
      resultPane.setPreferredSize(new Dimension(200, height));
      resultPane.setSize(new Dimension(200, height));
      revalidate();
    });

    clearButton.addActionListener((ActionEvent e) -> {
      nameField.setText("");
      filterField.setText("");
      filterComparisonField.setSelectedIndex(0);
      for (ResultItem r : results) {
        resultPane.remove(r);
      }
      resultPane.removeAll();
      resultPane.setPreferredSize(new Dimension(200, 35));
      resultPane.setSize(new Dimension(200, 55));
      results.clear();
      revalidate();
    });

    add(title);
    namePanel.setBorder(new EmptyBorder(2, 2, 2, 2));
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
    resultPane.setPreferredSize(new Dimension(250, 55));
    add(resultPane);
    setVisible(true);
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
    ResultItem(final String name, String description, final Object value) {
      setBackground(Color.WHITE);
      setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
      int height = 60;
      if (description.length() > 10) {
        description = description.substring(0, 11) + "<br/>" + description.substring(11, description.length());
        height += 20;
      }
      JLabel resultLabel = new JLabel("<html>Name: " + name +
          "<br/>" +
          "Description: " + description +
          "<br/>" +
          "Value: " + value +
          "</html>");
      setPreferredSize(new Dimension(250, height));
      setMaximumSize(new Dimension(250, height));
      add(resultLabel);
      setVisible(true);
    }
  }
}
