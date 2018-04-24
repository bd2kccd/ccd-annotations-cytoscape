package edu.pitt.cs.admt.cytoscape.annotations.task;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class ComponentHighlightTask extends AbstractTask {

  private static final String SELECTED = "selected";

  private final CyNetwork network;
  private final HashSet<Long> suids;

  private ComponentHighlightTask(final CyNetwork network, final Collection<Integer> suids) {
    this.network = network;
    this.suids = suids
        .stream()
        .map(Integer::longValue)
        .collect(Collectors.toCollection(HashSet::new));
  }

  public static ComponentHighlightTask CreateComponentHighlightTask(final CyNetwork network, final Collection<Integer> suids) {
    return new ComponentHighlightTask(network, suids);
  }

  public TaskIterator toTaskIterator() {
    return new TaskIterator(this);
  }

  public void run() {
    for (CyNode node: this.network.getNodeList()) {
      CyRow row = this.network.getRow(node);
      row.set(SELECTED, suids.contains(node.getSUID()));
    }

    for (CyEdge edge: this.network.getEdgeList()) {
      CyRow row = this.network.getRow(edge);
      row.set(SELECTED, suids.contains(edge.getSUID()));
    }
  }

  @Override
  public void run(TaskMonitor monitor) {
    run();
  }
}
