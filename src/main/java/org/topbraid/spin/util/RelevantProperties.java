package org.topbraid.spin.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.spin.internal.ObjectPropertiesGetter;
import org.topbraid.spin.model.Ask;
import org.topbraid.spin.model.Command;
import org.topbraid.spin.model.Construct;
import org.topbraid.spin.model.ElementList;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.model.QueryOrTemplateCall;
import org.topbraid.spin.model.SPINInstance;
import org.topbraid.spin.model.Template;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * Control logic that determines "relevant" properties for given classes or instances.
 * 
 * @author Holger Knublauch
 */
public class RelevantProperties {


	private static void addProperties(QueryOrTemplateCall qot, Set<Property> results) {
		Model model = qot.getCls().getModel();
		if(qot.getTemplateCall() != null) {
			TemplateCall templateCall = qot.getTemplateCall();
			Template template = templateCall.getTemplate();
			if(template != null) {
				Command spinQuery = template.getBody();
				if(spinQuery instanceof Ask || spinQuery instanceof Construct) {
					ElementList where = ((Query)spinQuery).getWhere();
					if(where != null) {  // Gracefully ignore queries that only have sp:text
						ObjectPropertiesGetter getter = new ObjectPropertiesGetter(model, where, templateCall.getArgumentsMapByProperties());
						getter.run();
						results.addAll(getter.getResults());
					}
				}
			}
		}
		else if(qot.getQuery() instanceof Ask || qot.getQuery() instanceof Construct) {
			ElementList where = qot.getQuery().getWhere();
			if(where != null) {
				ObjectPropertiesGetter getter = new ObjectPropertiesGetter(model, where, null);
				getter.run();
				results.addAll(getter.getResults());
			}				
		}
	}


	public static List<Property> getRelevantSHACLPropertiesOfClass(Resource cls) {
		if(SHACLUtil.exists(cls.getModel())) {
			return SHACLUtil.getPropertiesOfClass(cls);
		}
		else {
			return null;
		}
	}
	
	
	public static Collection<Property> getRelevantSHACLPropertiesOfInstance(Resource instance) {
		if(SHACLUtil.exists(instance.getModel())) {
			List<Resource> types = JenaUtil.getTypes(instance);
			if(types.isEmpty()) {
				Resource defaultType = SHACLUtil.getDefaultTemplateType(instance);
				if(defaultType != null) {
					List<Property> properties = getRelevantSHACLPropertiesOfClass(defaultType);
					properties.remove(RDF.type);
					return properties;
				}
			}
			if(types.size() == 1) {
				return getRelevantSHACLPropertiesOfClass(types.get(0));
			}
			else if(types.size() > 1) {
				Set<Property> results = new HashSet<Property>();
				for(Resource type : types) {
					results.addAll(getRelevantSHACLPropertiesOfClass(type));
				}
				return results;
			}
		}
		return null;
	}
	
	
	public static Set<Property> getRelevantPropertiesOfClass(Resource cls) {
		Set<Property> results = new HashSet<Property>();
		
		JenaUtil.setGraphReadOptimization(true);
		try {

			StmtIterator it = cls.getModel().listStatements(null, RDFS.domain, cls);
			while (it.hasNext()) {
				Resource subject = it.next().getSubject();
				if (subject.isURIResource()) {
					results.add(cls.getModel().getProperty(subject.getURI()));
					JenaUtil.addDomainlessSubProperties(subject, results, new HashSet<Resource>());
				}
			}

			for(Resource superClass : JenaUtil.getSuperClasses(cls)) {
				Statement s = superClass.getProperty(OWL.onProperty);
				if(s != null && s.getObject().isURIResource()) {
					results.add(cls.getModel().getProperty(s.getResource().getURI()));
				}
			}

			Set<Property> others = RelevantProperties.getRelevantSPINPropertiesOfClass(cls);
			if(others != null) {
				for(Property other : others) {
					results.add(other);
				}
			}

			Collection<Property> shacls = RelevantProperties.getRelevantSHACLPropertiesOfClass(cls);
			if(shacls != null) {
				for(Property other : shacls) {
					results.add(other);
				}
			}
		}
		finally {
			JenaUtil.setGraphReadOptimization(false);
		}
		
		return results;
	}


	public static Set<Property> getRelevantSPINPropertiesOfInstance(Resource root) {
		if(SP.exists(root.getModel())) {
			SPINInstance instance = root.as(SPINInstance.class);
			Set<Property> results = new HashSet<Property>();
			for(QueryOrTemplateCall qot : instance.getQueriesAndTemplateCalls(SPIN.constraint)) {
				addProperties(qot, results);
			}
			return results;
		}
		else {
			return null;
		}
	}


	public static Set<Property> getRelevantSPINPropertiesOfClass(Resource cls) {
		if(SP.exists(cls.getModel())) {
			List<QueryOrTemplateCall> qots = new ArrayList<QueryOrTemplateCall>();
			SPINUtil.addQueryOrTemplateCalls(cls, SPIN.constraint, qots);
			Set<Property> results = new HashSet<Property>();
			for(QueryOrTemplateCall qot : qots) {
				addProperties(qot, results);
			}
			return results;
		}
		else {
			return null;
		}
	}
}
