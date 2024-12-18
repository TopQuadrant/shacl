package org.topbraid.shacl.validation;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Node;
import org.topbraid.shacl.engine.Constraint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Statistical data about execution time collected during validation.
 *
 * @author Holger Knublauch
 */
public class ValidationProfile {

    private Map<Node, Long> componentCounts = new ConcurrentHashMap<>();

    private Map<Node, Long> componentDurations = new ConcurrentHashMap<>();

    private Map<Node, Integer> componentFNCs = new ConcurrentHashMap<>();

    private Map<Node, Long> componentVNCs = new ConcurrentHashMap<>();

    private Map<Node, Long> shapeCounts = new ConcurrentHashMap<>();

    private Map<Node, Long> shapeDurations = new ConcurrentHashMap<>();

    private Map<Node, Integer> shapeFNCs = new ConcurrentHashMap<>();

    private Map<Node, Long> shapeVNCs = new ConcurrentHashMap<>();


    public void record(long duration, int focusNodeCount, long valueNodeCount, Constraint constraint) {
        record(componentCounts, componentDurations, componentFNCs, componentVNCs, constraint.getComponent().asNode(), duration, focusNodeCount, valueNodeCount);
        record(shapeCounts, shapeDurations, shapeFNCs, shapeVNCs, constraint.getShape().getShapeResource().asNode(), duration, focusNodeCount, valueNodeCount);
    }


    private void record(Map<Node, Long> counts, Map<Node, Long> durations, Map<Node, Integer> focusNodeCounts, Map<Node, Long> valueNodeCounts, Node key, long duration, int focusNodeCount, long valueNodeCount) {

        long totalDuration = durations.computeIfAbsent(key, n -> 0L);
        totalDuration += duration;
        durations.put(key, totalDuration);

        Long count = counts.computeIfAbsent(key, n -> 0L);
        count++;
        counts.put(key, count);

        int fnc = focusNodeCounts.computeIfAbsent(key, n -> 0);
        fnc += focusNodeCount;
        focusNodeCounts.put(key, fnc);

        long vnc = valueNodeCounts.computeIfAbsent(key, n -> 0L);
        vnc += valueNodeCount;
        valueNodeCounts.put(key, vnc);
    }


    public JsonObject toJson() {
        JsonObject result = new JsonObject();

        JsonArray components = new JsonArray();
        for (Node component : componentCounts.keySet()) {
            JsonObject o = new JsonObject();
            o.put("uri", component.isURI() ? component.getURI() : "_:" + component.getBlankNodeLabel());
            o.put("calls", componentCounts.get(component));
            o.put("ms", componentDurations.get(component));
            o.put("focusNodes", componentFNCs.get(component));
            o.put("valueNodes", componentVNCs.get(component));
            components.add(o);
        }
        result.put("components", components);

        JsonArray shapes = new JsonArray();
        for (Node shape : shapeCounts.keySet()) {
            JsonObject o = new JsonObject();
            o.put("uri", shape.isURI() ? shape.getURI() : "_:" + shape.getBlankNodeLabel());
            o.put("calls", shapeCounts.get(shape));
            o.put("ms", shapeDurations.get(shape));
            o.put("focusNodes", shapeFNCs.get(shape));
            o.put("valueNodes", shapeVNCs.get(shape));
            shapes.add(o);
        }
        result.put("shapes", shapes);

        return result;
    }
}
