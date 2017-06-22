
var $shapes = null;
var $data = null;


module.exports.$shapes = function() {
    if (arguments.length === 0) {
        return $shapes
    } else {
        $shapes = arguments[0];
    }
};
module.exports.$data = function() {
    if (arguments.length === 0) {
        return $data
    } else {
        $data = arguments[0];
    }
};
