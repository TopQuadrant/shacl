

var extractValue = function(node, property) {
    var obj = node[property];
    if (obj) {
        return obj[0]["@value"];
    }
};

var extractId = function(node, property) {
    var obj = node[property];
    if (obj) {
        return obj[0]["@id"];
    }
};

var ValidationResult = function(resultNode, g) {
    this.graph = g;
    this.resultNode = resultNode;
};

ValidationResult.prototype.message = function() {
    return extractValue(this.resultNode, "http://www.w3.org/ns/shacl#resultMessage");
};

ValidationResult.prototype.path = function() {
    return extractId(this.resultNode, "http://www.w3.org/ns/shacl#resultPath");
};

ValidationResult.prototype.sourceConstraintComponent = function() {
    return extractId(this.resultNode, "http://www.w3.org/ns/shacl#sourceConstraintComponent");
};

ValidationResult.prototype.focusNode = function() {
    return extractId(this.resultNode, "http://www.w3.org/ns/shacl#focusNode");
};

ValidationResult.prototype.severity = function() {
    var severity = extractId(this.resultNode, "http://www.w3.org/ns/shacl#resultSeverity");
    if (severity != null) {
        return severity.split("#")[1];
    }
};

ValidationResult.prototype.sourceConstraintComponent = function() {
    return extractId(this.resultNode, "http://www.w3.org/ns/shacl#sourceConstraintComponent");
};

ValidationResult.prototype.sourceShape = function() {
    return extractId(this.resultNode, "http://www.w3.org/ns/shacl#sourceShape");
};

var ValidationReport = function(g) {
    this.graph = g;
    this.validationNode = null;
    for(var i=0; i<g.length; i++) {
        var conforms = g[i]["http://www.w3.org/ns/shacl#conforms"];
        if (conforms != null && conforms[0] != null) {
            this.validationNode = g[i];
            break;
        }
    }
    if (this.validationNode == null) {
        throw new Exception("Cannot find validation report node");
    }
};

ValidationReport.prototype.conforms = function() {
    var conforms = this.validationNode["http://www.w3.org/ns/shacl#conforms"][0];
    if (conforms != null) {
        return conforms["@value"] === "true";
    }
};

ValidationReport.prototype.results = function() {
    var results = this.validationNode["http://www.w3.org/ns/shacl#result"] || [];
    var that = this;
    return results.map(function(result) {
        return new ValidationResult(that.findNode(result["@id"]), this.graph);
    });
};

ValidationReport.prototype.findNode = function(id) {
    for (var i=0; i<this.graph.length; i++) {
        if (this.graph[i]["@id"] === id) {
            return this.graph[i];
        }
    }
};


module.exports = ValidationReport;