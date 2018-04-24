package edu.pitt.cs.admt.cytoscape.annotations.ui;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegate;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import edu.pitt.cs.admt.cytoscape.annotations.task.ComponentHighlightTaskFactory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.TaskManager;

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
  private final JLabel resultLabel = new JLabel();
  private final JComboBox<String> filterComparisonField = new JComboBox<>(
      new DefaultComboBoxModel<>(
          new Vector(Arrays.asList(new String[]{"", "equals", "not equals", "starts with", "ends with", ">", "≥", "<", "≤"}))
      )
  );
  private final JComboBox<String> componentSetSelection = new JComboBox<>(
      new DefaultComboBoxModel<>(
          new Vector(Arrays.asList(new String[]{"All", "Selected"}))
      )
  );
  private final JComboBox<String> annotationSetSelection = new JComboBox<>(
      new DefaultComboBoxModel<>(
          new Vector(Arrays.asList(new String[]{"Union", "Intersection"}))
      )
  );
  private Long networkSUID = null;
  private CyNetwork network = null;
  //  private List<String> annotationNames = new LinkedList<>();
//  private JPanel resultContainer = new JPanel(new GridLayout(0, 1));
  private JPanel resultPane = new JPanel();
  private JScrollPane resultScroll = new JScrollPane(resultPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  private Set<ResultItem> results = new LinkedHashSet<>();

  public SearchAnnotationPanel(final TaskManager taskManager, final ComponentHighlightTaskFactory highlightTaskFactory) {

    // panel settings
    setBorder(new EmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(300, 800));
    setMaximumSize(new Dimension(400, 1000));
    resultPane.setSize(new Dimension(250, 55));
    resultPane.setPreferredSize(new Dimension(250, 350));
    resultScroll.setSize(new Dimension(250, 350));
    resultScroll.setPreferredSize(new Dimension(250, 350));
    resultScroll.setMaximumSize(new Dimension(250, 350));
    annotationSetSelection.setEnabled(false);

    componentSetSelection.addActionListener((ActionEvent e) -> {
      int index = ((JComboBox) e.getSource()).getSelectedIndex();
      if (index == 0) {
        annotationSetSelection.setSelectedIndex(0);
        annotationSetSelection.setEnabled(false);
      } else {
        annotationSetSelection.setEnabled(true);
      }
    });

    // actions
    searchButton.addActionListener((ActionEvent e) -> {
      results.clear();
      resultPane.removeAll();
      resultPane.setSize(new Dimension(250, 55));

      Function<Object, Boolean> valueFilter = buildValueFilter();

      try {
        Collection<AnnotToEntity> res = buildSearchResults(valueFilter);
        HashMap<UUID, Annotation> annotationNameMap = new HashMap<>();
        for (AnnotToEntity r: res) {
          if (!annotationNameMap.containsKey(r.getAnnotationId())) {
            annotationNameMap.put(r.getAnnotationId(), StorageDelegate.getAnnotation(this.networkSUID, r.getAnnotationId()).get());
          }
          Annotation a = annotationNameMap.get(r.getAnnotationId());
          results.add(new ResultItem(this, taskManager, highlightTaskFactory, networkSUID, a, r));
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        return;
      }
      for (ResultItem r : results) {
        resultPane.add(r);
      }
      int height = 0;
      for (ResultItem r: results) {
        height += r.getMaximumSize().height + 5;
      }
      resultPane.setSize(new Dimension(250, height));
      resultPane.setPreferredSize(new Dimension(250, height));
      this.resultLabel.setText("Showing " + results.size() + " results");
      revalidate();
//      System.out.println("Processed " + results.size() + " results");
    });

    clearButton.addActionListener((ActionEvent e) -> {
      this.clearSelected();
      highlightTaskFactory.clearComponentHighlight().run();
      nameField.setText("");
      filterField.setText("");
      filterComparisonField.setSelectedIndex(0);
      componentSetSelection.setSelectedIndex(0);
      annotationSetSelection.setSelectedIndex(0);
      for (ResultItem r : results) {
        resultPane.remove(r);
      }
      resultPane.removeAll();
      resultPane.setSize(new Dimension(250, 55));
      results.clear();
      resultLabel.setText("Showing 0 results");
      SwingUtilities.invokeLater(() -> searchButton.doClick());
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
    add(componentSetSelection);
    add(annotationSetSelection);
    add(clearButton);
    add(searchButton);
    add(resultLabel);
    resultPane.setBackground(Color.WHITE);
    resultPane.setPreferredSize(new Dimension(250, 55));
    resultScroll.setViewportView(resultPane);
    add(resultScroll);
    setVisible(true);
  }

  public void refresh(CyNetwork network) {
    this.networkSUID = network.getSUID();
    this.network = network;
    SwingUtilities.invokeLater(() -> searchButton.doClick());
  }

  public void setResults(Set<ResultItem> results) {
    for (ResultItem item: this.results) {
      this.resultPane.remove(item);
    }
    this.resultPane.removeAll();
    this.results = results;
    int height = 0;
    for (ResultItem item: this.results) {
      height += item.getMaximumSize().height + 5;
      this.resultPane.add(item);
    }
    resultPane.setPreferredSize(new Dimension(200, height));
    resultPane.setSize(new Dimension(200, height));
    revalidate();
  }

  public void clearSelected() {
    for (ResultItem r: this.results) {
      r.deselect();
    }
  }

  private Collection<AnnotToEntity> buildSearchResults(Function<Object, Boolean> valueFilter) throws SQLException, IOException, ClassNotFoundException {
    String name = nameField.getText().toLowerCase();
    // start with union query
    Collection<AnnotToEntity> results = StorageDelegate.searchEntitiesWithPredicate(this.networkSUID, name, valueFilter);
    // filter out non-selected if set
    if (componentSetSelection.getSelectedIndex() == 1) {
      Set<Long> selected = CyTableUtil.getNodesInState(this.network, "SELECTED", true)
          .stream()
          .map(CyNode::getSUID)
          .collect(Collectors.toSet());
      selected.addAll(CyTableUtil.getEdgesInState(this.network, "SELECTED", true)
          .stream()
          .map(CyEdge::getSUID)
          .collect(Collectors.toSet()));
      results.removeIf(e -> !selected.contains(new Long(e.getEntityId())));
      // perform intersection if set (only on selected)
      if (annotationSetSelection.getSelectedIndex() == 1) {
        Map<UUID, Long> cyIdCount = results.stream().collect(
            groupingBy(AnnotToEntity::getCytoscapeAnnotationId, counting()));
        Long unique = results.stream().map(AnnotToEntity::getEntityId).distinct().count();
        results.removeIf(e -> cyIdCount.get(e.getCytoscapeAnnotationId()) < unique);
      }
    }
    return results;
  }

  private Function<Object, Boolean> buildValueFilter() {
    int filterComparator = filterComparisonField.getSelectedIndex();
    Object compare = null;
    if (filterComparator == 3 || filterComparator == 4) {
      compare = filterField.getText();
    }
    if (compare == null) {
      try {
        compare = Integer.parseInt(filterField.getText());
      } catch (Exception ex) {
      }
    }
    if (compare == null) {
      try {
        compare = Float.parseFloat(filterField.getText());
      } catch (Exception ex) { }
    }
    if (compare == null && (filterField.getText().equalsIgnoreCase("true") || filterField.getText().equalsIgnoreCase("false"))) {
      compare = Boolean.parseBoolean(filterField.getText());
    }
    // for chars (we probably won't use chars)
//      if (compare == null && filterField.getText().length() == 1) {
//        compare = filterField.getText().charAt(0);
//      }
    if (compare == null) {
      compare = filterField.getText();
    }
    final Object comparer = compare;
    switch (filterComparator) {
      case 1:   // equals
        if (comparer instanceof String) {
          return (value) -> value.toString().equals((String) comparer);
        } else if (comparer instanceof Integer) {
          return (value) ->
              (value instanceof Integer && Integer.compare((Integer) value, (Integer) comparer) == 0) ||
              (value instanceof Float && Float.compare((Float) value, new Float((Integer) comparer)) == 0);
        } else if (comparer instanceof Float) {
          return (value) ->
              (value instanceof Integer && Float.compare(new Float((Integer) value), (Float) comparer) == 0) ||
              (value instanceof Float && Float.compare((Float) value, (Float) comparer) == 0);
        } else if (comparer instanceof Boolean) {
          return (value) -> value instanceof Boolean && Boolean.compare((Boolean) value, (Boolean) comparer) == 0;
        } else {
          return (value) -> value == comparer;
        }
//        break;
      case 2: // not equals
        if (comparer instanceof String) {
          return (value) -> !value.toString().equals((String) comparer);
        } else if (comparer instanceof Integer) {
          return (value) ->
              (value instanceof Integer && Integer.compare((Integer) value, (Integer) comparer) != 0) ||
              (value instanceof Float && Float.compare((Float) value, new Float((Integer) comparer)) != 0);
        } else if (comparer instanceof Float) {
          return (value) ->
              (value instanceof Integer && Float.compare(new Float((Integer) value), (Float) comparer) != 0) ||
              (value instanceof Float && Float.compare((Float) value, (Float) comparer) != 0);
        } else if (comparer instanceof Boolean) {
          return (value) -> value instanceof Boolean && Boolean.compare((Boolean) value, (Boolean) comparer) != 0;
        } else {
          return (value) -> value != comparer;
        }
//        break;
      case 3: // starts with
        if (!(comparer instanceof String)) {
          return (value) -> false;
        }
        return (value) -> value instanceof String && ((String) value).startsWith((String) comparer);
//        break;
      case 4: // ends with
        if (!(comparer instanceof String)) {
          return (value) -> false;
        }
        return (value) -> value instanceof String && ((String) value).endsWith((String) comparer);
//        break;
      case 5: // greater than
        if (comparer instanceof String) {
          return (value) -> value.toString().compareTo((String) comparer) > 0;
        } else if (comparer instanceof Integer) {
          return (value) ->
              (value instanceof Integer && Integer.compare((Integer) value, (Integer) comparer) > 0) ||
              (value instanceof Float && Float.compare((Float) value, new Float((Integer) comparer)) > 0);
        } else if (comparer instanceof Float) {
          return (value) ->
              (value instanceof Integer && Float.compare(new Float((Integer) value), (Float) comparer) > 0) ||
              (value instanceof Float && Float.compare((Float) value, (Float) comparer) > 0);
        } else {
          return (value) -> false;
        }
//        break;
      case 6: // greater than or equal to
        if (comparer instanceof String) {
          return (value) -> value.toString().compareTo((String) comparer) >= 0;
        } else if (comparer instanceof Integer) {
          return (value) ->
              (value instanceof Integer && Integer.compare((Integer) value, (Integer) comparer) >= 0) ||
              (value instanceof Float && Float.compare((Float) value, new Float((Integer) comparer)) >= 0);
        } else if (comparer instanceof Float) {
          return (value) ->
              (value instanceof Integer && Float.compare(new Float((Integer) value), (Float) comparer) >= 0) ||
              (value instanceof Float && Float.compare((Float) value, (Float) comparer) >= 0);
        } else {
          return (value) -> false;
        }
//        break;
      case 7: // less than
        if (comparer instanceof String) {
          return (value) -> value.toString().compareTo((String) comparer) < 0;
        } else if (comparer instanceof Integer) {
          return (value) ->
              (value instanceof Integer && Integer.compare((Integer) value, (Integer) comparer) < 0) ||
                  (value instanceof Float && Float.compare((Float) value, new Float((Integer) comparer)) < 0);
        } else if (comparer instanceof Float) {
          return (value) ->
              (value instanceof Integer && Float.compare(new Float((Integer) value), (Float) comparer) < 0) ||
                  (value instanceof Float && Float.compare((Float) value, (Float) comparer) < 0);
        } else {
          return (value) -> false;
        }
//        break;
      case 8: // less than or equal to
        if (comparer instanceof String) {
          return (value) -> value.toString().compareTo((String) comparer) <= 0;
        } else if (comparer instanceof Integer) {
          return (value) ->
              (value instanceof Integer && Integer.compare((Integer) value, (Integer) comparer) <= 0) ||
                  (value instanceof Float && Float.compare((Float) value, new Float((Integer) comparer)) <= 0);
        } else if (comparer instanceof Float) {
          return (value) ->
              (value instanceof Integer && Float.compare(new Float((Integer) value), (Float) comparer) <= 0) ||
                  (value instanceof Float && Float.compare((Float) value, (Float) comparer) <= 0);
        } else {
          return (value) -> false;
        }
//        break;
      case 0: // no filter
      default:
        return (value) -> true;
//        break;
    }
  }

  private class ResultItem extends JPanel {

    private static final long serialVersionUID = 4810591818818214721L;

    private final SearchAnnotationPanel searchPanel;

    ResultItem(
        final SearchAnnotationPanel searchPanel,
        final TaskManager taskManager,
        final ComponentHighlightTaskFactory highlightTaskFactory,
        final Long network,
        final Annotation annotation,
        final AnnotToEntity annotToEntity) {
      this.searchPanel = searchPanel;
      setBackground(Color.WHITE);
      setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
      int height;

      String description = annotation.getDescription();
      String name = annotation.getName();
      Object value = annotToEntity.getValue();
      if (description != null && description.length() > 10) {
        if (description.length() <= 25) {
          description = description.substring(0, 11) + "<br/>" + description
              .substring(11, description.length());
          height = 80;
        } else {
          StringBuilder fmtDescription = new StringBuilder(description.substring(0, 11));
          fmtDescription.append("<br/>");
          int limit = 25;
          int parts = description.length() / (int) limit;
          for (int i = 0; i < parts; i++) {
            int min = Math.min(11 + limit * (i + 1), description.length());
            fmtDescription.append(description.substring(11 + limit * i, min));
            if (min != description.length()) {
              fmtDescription.append("<br/>");
            }
          }
          if (description.length() > 11 + limit * parts) {
            fmtDescription.append(description.substring(11 + limit * parts, description.length()));
          }
          description = fmtDescription.toString();
          height = 80 + 10 * parts;
        }
      } else if (description != null) {
        description = String.format("%1$-10s", description);
        height = 60;
      } else {
        height = 60;
      }

      setPreferredSize(new Dimension(200, height));
      setMaximumSize(new Dimension(200, height));

      String descriptionLabel = description != null ? "Description: " + description + "<br/>" : "";
      JLabel resultLabel = new JLabel("<html>Name: " + name +
          "<br/>" +
          descriptionLabel +
          "Value: " + value +
          "</html>");
      add(resultLabel);
      setVisible(true);

      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          // not currently using this; too specific
          // click must not be drag, released outside of panel, etc.
          super.mouseClicked(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
          super.mousePressed(e);
          ResultItem item = (ResultItem) e.getComponent();
          item.getSearchPanel().clearSelected();
          item.select();
          UUID cyId = annotToEntity.getCytoscapeAnnotationId();
          try {
            Collection<Integer> components = StorageDelegate.getNetworkComponentsOnCytoscapeAnnotUUID(networkSUID, cyId);
            highlightTaskFactory.createComponentHighlightTask(components).run();
          } catch (SQLException ex) {
            ex.printStackTrace();
          }
        }
      });
    }

    public void select() {
      this.setBackground(Color.YELLOW);
    }

    public void deselect() {
      this.setBackground(Color.WHITE);
    }

    public SearchAnnotationPanel getSearchPanel() {
      return this.searchPanel;
    }
  }
}
