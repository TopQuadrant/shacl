package org.topbraid.shacl.expr;

public class AppendContext {
	
	private int indentation;

	private StringBuffer sb;
	
	private int varIndex;
	
	
	public AppendContext(StringBuffer sb) {
		this.sb = sb;
	}
	
	
	public void append(String str) {
		sb.append(str);
	}
	
	
	public void decreaseIndent() {
		indentation--;
	}
	
	
	public String getNextVarName() {
		return "" + (char)('a' + varIndex++);
	}
	
	
	public void increaseIndent() {
		indentation++;
	}
	
	
	public void indent() {
		for(int i = 0; i < indentation; i++) {
			sb.append("    ");
		}
	}
}
