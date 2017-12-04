package edu.pitt.cs.admt.cytoscape.annotations.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.*;

import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegate;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class CCDControlPanel extends JPanel implements CytoPanelComponent, Serializable {

    private static final long serialVersionUID = 7128778486978079375L;

    private static final String CCD_ANNOTATION_ATTRIBUTE = "__CCD_Annotations";
    private static final String ANNOTATION_SET_ATTRIBUTE = "__Annotation_Set";

    private final CyApplicationManager cyApplicationManager;
    private final CyNetworkViewManager networkViewManager;
    private final AnnotationManager annotationManager;
    private final AnnotationFactory<TextAnnotation> annotationFactory;
    private final StorageDelegate storageDelegate;
    private JLabel annotationsList;

    public CCDControlPanel(final CyApplicationManager cyApplicationManager, final CyNetworkViewManager networkViewManager, final AnnotationManager annotationManager, final AnnotationFactory<TextAnnotation> annotationFactory, final StorageDelegate storageDelegate) {
        this.cyApplicationManager = cyApplicationManager;
        this.networkViewManager = networkViewManager;
        this.annotationManager = annotationManager;
        this.annotationFactory = annotationFactory;
        this.storageDelegate = storageDelegate;
        // title
        JLabel label = new JLabel("New CCD Annotation\n", SwingConstants.CENTER);

        // extended attribute selection
        final String[] attributeOptions = { "Comment", "Posterior Probability" };
        final SpinnerListModel listModel = new SpinnerListModel(attributeOptions);
        final JSpinner attributeSpinner = new JSpinner(listModel);
        ((JSpinner.DefaultEditor) attributeSpinner.getEditor()).getTextField().setEditable(false);
        ((JSpinner.DefaultEditor) attributeSpinner.getEditor()).getTextField().setPreferredSize(new Dimension(150, 20));

        // annotation data
        JTextArea annotationText = new JTextArea("CCD annotation text");
        annotationText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent e) {
                if(annotationText.getText().equals("CCD annotation text")) {
                    annotationText.setText("");
                }
            }

            @Override
            public void focusLost(final FocusEvent e) {
                if(annotationText.getText().isEmpty()) {
                    annotationText.setText("CCD annotation text");
                }
            }
        });
        annotationText.setPreferredSize(new Dimension(300, 100));
        annotationText.setLineWrap(true);
        annotationsList = new JLabel("");
        JButton button = new JButton("Create");

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CyNetworkView networkView = networkViewManager.getNetworkViewSet().iterator().next();
                List<CyNode> nodes = CyTableUtil.getNodesInState(cyApplicationManager.getCurrentNetwork(), "selected", true);
                List<CyEdge> edges = CyTableUtil.getEdgesInState(cyApplicationManager.getCurrentNetwork(), "selected", true);
                if (nodes.isEmpty() && edges.isEmpty()) {
                    annotationsList.setText("Must select node or edge");
                } else {
                    CyNetworkView cyNetworkView = cyApplicationManager.getCurrentNetworkView();
                    Double x = 0.0;
                    int xCount = 0;
                    Double y = 0.0;
                    int yCount = 0;
                    for (CyNode node: nodes) {
                        View<CyNode> cyNodeView = cyNetworkView.getNodeView(node);
                        x += cyNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
                        xCount++;
                        y += cyNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
                        yCount++;
                    }
                    for (CyEdge edge: edges) {
                        x += cyNetworkView.getNodeView(edge.getSource()).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
                        x += cyNetworkView.getNodeView(edge.getTarget()).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
                        xCount += 2;
                        y += cyNetworkView.getNodeView(edge.getSource()).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
                        y += cyNetworkView.getNodeView(edge.getTarget()).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
                        yCount += 2;
                    }
                    Map<String, String> args = new HashMap<>();
                    args.put("x", String.valueOf(x/xCount));
                    args.put("y", String.valueOf(y/yCount));
                    args.put("zoom", String.valueOf(1.0));
                    args.put("fontFamily", "Arial");
                    args.put("color", String.valueOf(-16777216));
                    args.put("canvas", "foreground");
                    args.put("text", annotationText.getText());
                    TextAnnotation annotation = annotationFactory.createAnnotation(TextAnnotation.class, networkView, args);
                    annotationManager.addAnnotation(annotation);
                    addToTableColumn(cyApplicationManager.getCurrentNetwork(), annotation, nodes, edges);
                    annotationText.setText("CCD annotation text");
//                    annotationsList.setText("Added: " + annotationText.getText());
                }
            }
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
                if(searchText.getText().equals("Search")) {
                    searchText.setText("");
                }
            }

            @Override
            public void focusLost(final FocusEvent e) {
                if(searchText.getText().isEmpty()) {
                    searchText.setText("Search");
                }
            }
        });

        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");

        searchButton.addActionListener(new SearchActionListener(this.storageDelegate, searchText.getText()));

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Clearing...");
            }
        });

        // Java FX Experiment
        final JFXPanel fxPanel = new JFXPanel();
        final Label fxSearchLabel = new Label("\nSearch\n");
        StackPane holder = new StackPane();
        Scene scene = new Scene(new Group());
        holder.getChildren().add(fxSearchLabel);
        holder.setStyle("-fx-background-color: #EEEEEE");
        ((Group)scene.getRoot()).getChildren().add(holder);
        JScrollPane scrollPane = new JScrollPane(annotationText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
        this.add(fxPanel);
        fxPanel.setScene(scene);
        this.add(searchLabel);
        this.add(searchText);
        this.add(searchButton);
        this.add(clearButton);
        this.setVisible(true);
    }

    public void addToTableColumn(final CyNetwork cyNetwork,
                                 final TextAnnotation annotation,
                                 final List<CyNode> nodes,
                                 final List<CyEdge> edges) {
        // add annotation to network
        List<String> row = cyNetwork.getRow(cyNetwork, CyNetwork.LOCAL_ATTRS).getList(CCD_ANNOTATION_ATTRIBUTE, String.class);
        row.add(annotation.getUUID().toString() + "|" + annotation.getText());
        cyNetwork.getRow(cyNetwork, CyNetwork.LOCAL_ATTRS).set(CCD_ANNOTATION_ATTRIBUTE, row);

        // add annotation to nodes
        for (CyNode node: nodes) {
            List<String> nodeRow = cyNetwork.getRow(node).getList(ANNOTATION_SET_ATTRIBUTE, String.class);
            nodeRow.add(annotation.getUUID().toString());
            Set<String> nodeSet = new HashSet<>(nodeRow);
            cyNetwork.getRow(node).set(ANNOTATION_SET_ATTRIBUTE, new ArrayList<>(nodeSet));
        }

        // add annotation to edges
        for (CyEdge edge: edges) {
            List<String> edgeRow = cyNetwork.getRow(edge).getList(ANNOTATION_SET_ATTRIBUTE, String.class);
            edgeRow.add(annotation.getUUID().toString());
            Set<String> edgeSet = new HashSet<>(edgeRow);
            cyNetwork.getRow(edge).set(ANNOTATION_SET_ATTRIBUTE, new ArrayList<>(edgeSet));
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

    public class SearchActionListener implements ActionListener {
        private StorageDelegate storageDelegate;
        private String searchString;

        public SearchActionListener(final StorageDelegate storageDelegate, final String searchString) {
            super();
            System.out.println("Creating action listener");
            System.out.println("search string: " + searchString);
            this.storageDelegate = new StorageDelegate();
            this.searchString = searchString;
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Running action performed");
            try {
                Collection<AnnotToEntity> result = this.storageDelegate.searchAnnotations(this.searchString);
                for (AnnotToEntity entity: result) {
                    System.out.println(entity.getValue());
                }
                this.storageDelegate.close();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}
