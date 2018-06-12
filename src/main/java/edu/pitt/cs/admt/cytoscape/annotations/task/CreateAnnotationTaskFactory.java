package edu.pitt.cs.admt.cytoscape.annotations.task;

import java.util.Collection;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class CreateAnnotationTaskFactory extends AbstractTaskFactory {

  private final CyApplicationManager applicationManager;
  private final AnnotationManager annotationManager;
  private final AnnotationFactory<TextAnnotation> annotationFactory;
  private final TaskManager taskManager;

  public CreateAnnotationTaskFactory(final CyApplicationManager applicationManager,
      final AnnotationManager annotationManager,
      final AnnotationFactory<TextAnnotation> annotationFactory,
      final TaskManager taskManager) {
    this.applicationManager = applicationManager;
    this.annotationManager = annotationManager;
    this.annotationFactory = annotationFactory;
    this.taskManager = taskManager;
  }

  @Override
  /**
   * Do not use this method. Overriding it is required to extend AbstractTaskFactory.
   * @see org.cytoscape.work.AbstractTaskFactory;
   * Use the following instead:
   *             {@link #createTaskIterator(CreateAnnotationTask)}
   */
  public TaskIterator createTaskIterator() {
    return new TaskIterator();
  }

  public CreateAnnotationTask createOnSelected(final String name) {
    return CreateAnnotationTask.createAnnotationTaskOnSelected(
        this.applicationManager,
        this.annotationManager,
        this.annotationFactory,
        this.taskManager,
        name);
  }

  public CreateAnnotationTask createOnNodes(final String name, final Collection<CyNode> nodes) {
    return CreateAnnotationTask.createAnnotationTaskOnNodes(
        this.applicationManager,
        this.annotationManager,
        this.annotationFactory,
        this.taskManager,
        name,
        nodes);
  }

  public CreateAnnotationTask createOnEdges(final String name, final Collection<CyEdge> edges) {
    return CreateAnnotationTask.createAnnotationTaskOnEdges(
        this.applicationManager,
        this.annotationManager,
        this.annotationFactory,
        this.taskManager,
        name,
        edges);
  }

  public CreateAnnotationTask createOnNodesAndEdges(final String name,
      final Collection<CyNode> nodes,
      final Collection<CyEdge> edges) {
    return CreateAnnotationTask.createAnnotationTaskOnNodesAndEdges(
        this.applicationManager,
        this.annotationManager,
        this.annotationFactory,
        this.taskManager,
        name,
        nodes,
        edges);
  }

  public TaskIterator createTaskIterator(final CreateAnnotationTask createAnnotationTask) {
    return new TaskIterator(createAnnotationTask);
  }
}
