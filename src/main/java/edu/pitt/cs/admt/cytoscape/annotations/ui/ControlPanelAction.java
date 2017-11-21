package edu.pitt.cs.admt.cytoscape.annotations.ui;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class ControlPanelAction extends AbstractCyAction {
    private CySwingApplication application;
    private final CytoPanel cytoPanel;
    private CCDControlPanel controlPanel;

    public ControlPanelAction(CySwingApplication application, CCDControlPanel controlPanel) {
        super("Control Panel");
        this.application = application;
        this.cytoPanel = this.application.getCytoPanel(CytoPanelName.WEST);
        this.controlPanel = controlPanel;
    }

    public void actionPerformed(ActionEvent e) {
        if (cytoPanel.getState() == CytoPanelState.HIDE) {
            cytoPanel.setState(CytoPanelState.DOCK);
        }

        int index = cytoPanel.indexOfComponent(controlPanel);
        if (index == -1) {
            return;
        }
        cytoPanel.setSelectedIndex(index);
    }
}
