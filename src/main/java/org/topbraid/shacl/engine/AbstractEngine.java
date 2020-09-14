package org.topbraid.shacl.engine;

import java.net.URI;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.jenax.progress.ProgressMonitor;
import org.topbraid.shacl.entailment.SHACLEntailment;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.validation.DefaultShapesGraphProvider;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Base class for validation and rule engines.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractEngine implements NodeExpressionContext {
	
	protected Dataset dataset;
	
	protected ProgressMonitor monitor;

	protected ShapesGraph shapesGraph;
	
	protected URI shapesGraphURI;
	
	private Model shapesModel;

	
	protected AbstractEngine(Dataset dataset, ShapesGraph shapesGraph, URI shapesGraphURI) {
		if(shapesGraphURI == null) {
			shapesGraphURI = DefaultShapesGraphProvider.get().getDefaultShapesGraphURI(dataset);
		}
		this.dataset = dataset;
		this.shapesGraph = shapesGraph;
		this.shapesGraphURI = shapesGraphURI;
	}
	
	
	/**
	 * Ensures that the data graph includes any entailed triples inferred by the regime
	 * specified using sh:entailment in the shapes graph.
	 * Should be called prior to validation.
	 * Throws an Exception if unsupported entailments are found.
	 * If multiple sh:entailments are present then their order is undefined but they all get applied.
	 * @throws InterruptedException if the monitor has canceled it
	 */
	public void applyEntailments() throws InterruptedException {
		Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
		for(RDFNode ent : shapesModel.listObjectsOfProperty(SH.entailment).toList()) {
			if(ent.isURIResource()) {
				if(SHACLEntailment.get().getEngine(ent.asResource().getURI()) != null) {
					this.dataset = SHACLEntailment.get().withEntailment(dataset, shapesGraphURI, shapesGraph, ent.asResource(), monitor);
				}
				else {
					throw new UnsupportedOperationException("Unsupported entailment regime " + ent);
				}
			}
		}
	}
	
	
	public void checkCanceled() {
		if(monitor != null && monitor.isCanceled()) {
			throw new SHACLCanceledException();
		}
	}
	
	
	@Override
    public Dataset getDataset() {
		return dataset;
	}

	
	public ProgressMonitor getProgressMonitor() {
		return monitor;
	}
	
	
	@Override
    public ShapesGraph getShapesGraph() {
		return shapesGraph;
	}
	
	
	@Override
    public URI getShapesGraphURI() {
		return shapesGraphURI;
	}
	
	
	public Model getShapesModel() {
		if(shapesModel == null) {
			shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
		}
		return shapesModel;
	}

	
	public void setProgressMonitor(ProgressMonitor value) {
		this.monitor = value;
	}
}
