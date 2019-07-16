function simple($value) {
	if(!$value.isURI()) {
		return "IRIs expected";
	}
}