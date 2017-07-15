// class NodeSet
// (a super-primitive implementation for now!)

var NodeSet = function () {
    this.values = [];
};

NodeSet.prototype.add = function (node) {
    if (!this.contains(node)) {
        this.values.push(node);
    }
};

NodeSet.prototype.addAll = function (nodes) {
    for (var i = 0; i < nodes.length; i++) {
        this.add(nodes[i]);
    }
};

NodeSet.prototype.contains = function (node) {
    for (var i = 0; i < this.values.length; i++) {
        if (this.values[i].equals(node)) {
            return true;
        }
    }
    return false;
};

NodeSet.prototype.forEach = function (callback) {
    for (var i = 0; i < this.values.length; i++) {
        callback(this.values[i]);
    }
};

NodeSet.prototype.size = function () {
    return this.values.length;
};

NodeSet.prototype.toArray = function () {
    return this.values;
};

NodeSet.prototype.toString = function () {
    var str = "NodeSet(" + this.size() + "): [";
    var arr = this.toArray();
    for (var i = 0; i < arr.length; i++) {
        if (i > 0) {
            str += ", ";
        }
        str += arr[i];
    }
    return str + "]";
};

module.exports = NodeSet;