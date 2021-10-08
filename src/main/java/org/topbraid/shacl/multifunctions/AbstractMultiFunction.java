package org.topbraid.shacl.multifunctions;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.util.JenaNodeUtil;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.util.OrderThenPathLocalNameComparator;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Abstract base class suitable for all MultiFunction implementations.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractMultiFunction implements MultiFunction {
	
	private Node apiStatus;
	
	private String description;
	
	private List<MultiFunctionParameter> parameters;
	
	private List<MultiFunctionParameter> resultVars;

	private String uri;
	

	/**
	 * Use this constructor for dynamically declared MultiFunctions from RDF graphs in the workspace.
	 * @param uri  the URI of the multi-function
	 * @param declaration  the RDF resource with the declaration
	 */
	protected AbstractMultiFunction(String uri, Resource declaration) {
		
		Objects.requireNonNull(uri, "uri");
		this.uri = uri;
		
		Objects.requireNonNull(declaration, "declaration");
		initFrom(declaration);
	}
	

	/**
	 * Use this constructor for natively implemented MultiFunctions.
	 * The main difference is that these are programmatically generated before the RDF declaration is processed.
	 * For (corner) cases in which a workspace is broken and those files are not found, we at least declare the
	 * variable names so that the function can still execute correctly.
	 * @param uri  the URI of the multi-function
	 * @param argVarNames  the names of the parameters
	 * @param resultVarNames  the names of the result variables
	 */
	protected AbstractMultiFunction(String uri, List<String> argVarNames, List<String> resultVarNames) {
		Objects.requireNonNull(uri, "uri");
		this.uri = uri;

		this.parameters = argVarNames.stream().map(varName -> new MultiFunctionParameter(varName, null, true, null)).collect(Collectors.toList());
		this.resultVars = resultVarNames.stream().map(varName -> new MultiFunctionParameter(varName, null, true, null)).collect(Collectors.toList());
	}


	private static List<MultiFunctionParameter> createParameters(Resource declaration, Property predicate) {
		List<Resource> params = JenaUtil.getResourceProperties(declaration, predicate);
		Collections.sort(params, OrderThenPathLocalNameComparator.get());
		List<MultiFunctionParameter> parameters = params.stream().
				map(param -> MultiFunctionParameter.create(param)).
				collect(Collectors.toList());
		return parameters;
	}

	
	@Override
	public Node getAPIStatus() {
		return apiStatus;
	}

	
	@Override
	public String getDescription() {
		return description;
	}

	
	@Override
	public List<MultiFunctionParameter> getParameters() {
		return parameters;
	}

	
	@Override
	public List<MultiFunctionParameter> getResultVars() {
		return resultVars;
	}

	
	@Override
	public String getURI() {
		return uri;
	}

	
	public void initFrom(Resource declaration) {
		
		this.apiStatus = JenaNodeUtil.getObject(declaration.asNode(), DASH.apiStatus.asNode(), declaration.getModel().getGraph());

		this.description = JenaUtil.getStringProperty(declaration, RDFS.comment);
		
		this.parameters = createParameters(declaration, SH.parameter);
		
		this.resultVars = createParameters(declaration, DASH.resultVariable);
		if(resultVars.isEmpty()) {
			throw new IllegalArgumentException("A multi-function requires at least one result variable");
		}
	}
}
