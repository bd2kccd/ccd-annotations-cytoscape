package edu.pitt.cs.admt.cytoscape.annotations;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class CCDControlPanel extends JPanel implements CytoPanelComponent {

    private final CyNetworkViewManager networkViewManager;
    private final AnnotationManager annotationManager;
    private final AnnotationFactory<TextAnnotation> annotationFactory;
    private String annotationText = "";
    private JLabel annotationsList;

    public CCDControlPanel(final CyNetworkViewManager networkViewManager, final AnnotationManager annotationManager, final AnnotationFactory<TextAnnotation> annotationFactory) {
        this.networkViewManager = networkViewManager;
        this.annotationManager = annotationManager;
        this.annotationFactory = annotationFactory;
        JLabel label = new JLabel("New Annotation\n");
        annotationsList = new JLabel("");
        SpinnerNumberModel numberModel = new SpinnerNumberModel(
                new Integer(20),    // value
                new Integer(0),     // min
                new Integer(100),   // max
                new Integer(1)      // step
        );
        final JSpinner numberChooser = new JSpinner(numberModel);
        JButton button = new JButton("Create");

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CyNetworkView networkView = networkViewManager.getNetworkViewSet().iterator().next();
                String value = numberChooser.getValue().toString();
                Map<String, String> args = new HashMap<>();
                args.put("x", String.valueOf(-323.50));
                args.put("y", String.valueOf(-120.25));
                args.put("zoom", String.valueOf(1.0));
                args.put("fontFamily", "Arial");
                args.put("color", String.valueOf(-16777216));
                args.put("canvas", "foreground");
                args.put("text", value);
                Annotation annotation = annotationFactory.createAnnotation(TextAnnotation.class, networkView, args);
                annotationManager.addAnnotation(annotation);
                annotationText = "Last added: " + value;
                annotationsList.setText(annotationText);
            }
        });

        this.add(label);
        this.add(numberChooser);
        this.add(button);
        this.add(new JLabel(""));   // line break
        this.add(annotationsList);
        this.setVisible(true);
    }

    public Component getComponent() {
        return this;
    }

    public CytoPanelName getCytoPanelName() {
        return CytoPanelName.WEST;
    }

    public String getTitle() {
        return "Annotations";
    }

    public Icon getIcon() {
        return null;
    }
}
