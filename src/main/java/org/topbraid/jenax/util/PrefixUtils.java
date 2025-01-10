package org.topbraid.jenax.util;

import java.util.Map;

import org.apache.jena.graph.Graph;
import org.apache.jena.shared.PrefixMapping;

public class PrefixUtils {

    /**
     * Make the {@code dstGraph} prefix map the same {@code srcGraph} prefix map,
     * only making changes where necessary.
     * @param dstGraph  the destination graph
     * @param srcGraph  the source graph
     * @return false if no changes where made.
     */ 
    public static boolean alignPrefixMap(Graph dstGraph, Graph srcGraph) {
        boolean changeMade1 = copyPrefixMap(dstGraph, srcGraph);
        boolean changeMade2 = removeMissingPrefixes(dstGraph, srcGraph);
        return changeMade1 | changeMade2 ;
    }

   
    /** 
     * Copy prefix mappings into {@code dstGraph} from {@code srcGraph},
     * checking whether the prefix mapping is already set.
     * @param dstGraph  the destination graph
     * @param srcGraph  the source graph
     * @return false if no changes where made.
     */
    public static boolean copyPrefixMap(Graph dstGraph, Graph srcGraph) {
        boolean changeMade = false;
        PrefixMapping dstPM = dstGraph.getPrefixMapping();
        PrefixMapping srcPM = srcGraph.getPrefixMapping();
        // Copy different prefix mappings.
        for (Map.Entry<String, String> e : srcPM.getNsPrefixMap().entrySet()) {
            if (!e.getValue().equals(dstPM.getNsPrefixURI(e.getKey()))) {
                dstPM.setNsPrefix(e.getKey(), e.getValue());
                changeMade = true;
            }
        }
        return changeMade;
    }


    /** 
     * Remove prefix mappings from {@code dstGraph} that do not exist in {@code srcGraph}.
     * @param dstGraph  the destination graph
     * @param srcGraph  the source graph
     * @return false if no changes where made.
     */
    public static boolean removeMissingPrefixes(Graph dstGraph, Graph srcGraph) {
        boolean changeMade = false;
        PrefixMapping dstPM = dstGraph.getPrefixMapping();
        PrefixMapping srcPM = srcGraph.getPrefixMapping();
        // Delete those from dstPM that are not in srcPM
        for(String prefix : dstPM.getNsPrefixMap().keySet()) {
            if(srcPM.getNsPrefixURI(prefix) == null) {
                dstPM.removeNsPrefix(prefix);
                changeMade = true;
            }
        }
        return changeMade;
    }
}
