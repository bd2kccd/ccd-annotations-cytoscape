package edu.pitt.cs.admt.cytoscape.annotations.task;

import static edu.pitt.cs.admt.cytoscape.annotations.task.CreateAnnotationTask.createAnnotationTaskOnEdges;
import static edu.pitt.cs.admt.cytoscape.annotations.task.CreateAnnotationTask.createAnnotationTaskOnNodes;
import static edu.pitt.cs.admt.cytoscape.annotations.task.CreateAnnotationTask.createAnnotationTaskOnNodesAndEdges;
import static edu.pitt.cs.admt.cytoscape.annotations.task.CreateAnnotationTask.createAnnotationTaskOnSelected;

import java.util.Collection;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class CreateAnnotationTaskFactory extends AbstractTaskFactory {
    private final CyApplicationManager applicationManager;
    private final AnnotationManager annotationManager;
    private final AnnotationFactory<TextAnnotation> annotationFactory;

    public CreateAnnotationTaskFactory(final CyApplicationManager applicationManager,
                                       final AnnotationManager annotationManager,
                                       final AnnotationFactory<TextAnnotation> annotationFactory) {
        this.applicationManager = applicationManager;
        this.annotationManager = annotationManager;
        this.annotationFactory = annotationFactory;
    }

    @Override
    /**
     * Do not use this method. It must be overwritten to extend AbstractTaskFactory.
     * @see org.cytoscape.work.AbstractTaskFactory;
     * @deprecated Try using one of the following instead:
     *             {@link #createTaskIteratorAnnotationOnSelected(String)}
     *             {@link #createTaskIteratorAnnotationOnNodes(String, Collection)}
     *             {@link #createTaskIteratorAnnotationOnEdges(String, Collection)}
     *             {@link #createTaskIteratorAnnotationOnNodesAndEdge(String, Collection, Collection)}
     */
    public TaskIterator createTaskIterator() {
        return null;
    }

    public TaskIterator createTaskIteratorAnnotationOnSelected(final String annotationName) {
        return new TaskIterator(
                createAnnotationTaskOnSelected(this.applicationManager,
                        this.annotationManager,
                        this.annotationFactory,
                        annotationName)
        );
    }

    public TaskIterator createTaskIteratorAnnotationOnNodes(final String annotationName,
                                                            final Collection<CyNode> nodes) {
        return new TaskIterator(
                createAnnotationTaskOnNodes(this.applicationManager,
                        this.annotationManager,
                        this.annotationFactory,
                        annotationName,
                        nodes)
        );
    }

    public TaskIterator createTaskIteratorAnnotationOnEdges(final String annotationName,
                                                            final Collection<CyEdge> edges) {
        return new TaskIterator(
                createAnnotationTaskOnEdges(this.applicationManager,
                        this.annotationManager,
                        this.annotationFactory,
                        annotationName,
                        edges)
        );
    }

    public TaskIterator createTaskIteratorAnnotationOnNodesAndEdge(final String annotationName,
                                                                   final Collection<CyNode> nodes,
                                                                   final Collection<CyEdge> edges) {
        return new TaskIterator(
                createAnnotationTaskOnNodesAndEdges(this.applicationManager,
                        this.annotationManager,
                        this.annotationFactory,
                        annotationName,
                        nodes,
                        edges)
        );
    }
}
