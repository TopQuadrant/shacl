function propertyShape(focusNode, path, minCardinality) {
	var it = $data.find(focusNode, path, null);
	var count = 0;
	for(var t = it.next(); t; t = it.next()) {
		count++;
	}
	return count >= minCardinality.value;
}