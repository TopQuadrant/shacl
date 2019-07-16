function booleanFunction() {
	return true;
}

function floatFunction() {
	return 4.2;
}

function integerFunction() {
	return 42;
}

function nodeFunction() {
	return TermFactory.namedNode("http://aldi.de");
}

function stringFunction() {
	return "Hello";
}

function withArguments($arg1, $arg2) {
	return TermFactory.namedNode("http://aldi.de/product_" + $arg1.value + "_" + $arg2.value);
}
