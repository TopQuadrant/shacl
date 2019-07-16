function propertyShape($this, $path, $minCardinality) {
	var it = $data.find($this, $path, null);
	var count = 0;
	for(var t = it.next(); t; t = it.next()) {
		count++;
	}
	return count >= $minCardinality.lex;
}