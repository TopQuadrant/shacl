package org.topbraid.shacl.expr;

public abstract class ComplexNodeExpression extends NodeExpression {
	
	public abstract void appendLabel(AppendContext context, String targetVarName);
	
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		AppendContext context = new AppendContext(sb);
		context.append("{\n");
		context.increaseIndent();
		appendLabel(context, "result");
		context.decreaseIndent();
		context.append("}");
		return sb.toString();
	}
}
