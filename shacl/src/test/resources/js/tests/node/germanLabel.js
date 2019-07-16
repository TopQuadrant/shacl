function germanLabel($value) {
	var results = [];
	var p = TermFactory.namedNode("http://example.org/ns#germanLabel");
	var s = $data.find($value, p, null);
	for(var t = s.next(); t; t = s.next()) {
		var object = t.object;
		if(object.termType != "Literal" || !object.language.startsWith("de")) {
			results.push({
				value : object
			});
		}
	}
	return results;
}
