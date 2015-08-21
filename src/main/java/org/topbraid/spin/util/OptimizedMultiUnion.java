package org.topbraid.spin.util;

import java.util.List;

import com.hp.hpl.jena.graph.Node;

/**
 * A Graph interface providing additional optimization features.
 * 
 * @author Holger Knublauch
 */
public interface OptimizedMultiUnion {
	
	boolean getIncludesSHACL();
	
	
	boolean getIncludesSPIN();
	
	
	List<Node> getLabelProperties();
}
