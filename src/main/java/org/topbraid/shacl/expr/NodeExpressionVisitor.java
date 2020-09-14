package org.topbraid.shacl.expr;

import org.topbraid.shacl.expr.lib.AskExpression;
import org.topbraid.shacl.expr.lib.ConstantTermExpression;
import org.topbraid.shacl.expr.lib.CountExpression;
import org.topbraid.shacl.expr.lib.DistinctExpression;
import org.topbraid.shacl.expr.lib.ExistsExpression;
import org.topbraid.shacl.expr.lib.FilterShapeExpression;
import org.topbraid.shacl.expr.lib.FocusNodeExpression;
import org.topbraid.shacl.expr.lib.FunctionExpression;
import org.topbraid.shacl.expr.lib.GroupConcatExpression;
import org.topbraid.shacl.expr.lib.IfExpression;
import org.topbraid.shacl.expr.lib.IntersectionExpression;
import org.topbraid.shacl.expr.lib.LimitExpression;
import org.topbraid.shacl.expr.lib.MaxExpression;
import org.topbraid.shacl.expr.lib.MinExpression;
import org.topbraid.shacl.expr.lib.MinusExpression;
import org.topbraid.shacl.expr.lib.OffsetExpression;
import org.topbraid.shacl.expr.lib.OrderByExpression;
import org.topbraid.shacl.expr.lib.PathExpression;
import org.topbraid.shacl.expr.lib.SelectExpression;
import org.topbraid.shacl.expr.lib.SumExpression;
import org.topbraid.shacl.expr.lib.UnionExpression;

public interface NodeExpressionVisitor {
	
	void visit(AskExpression expr);
	
	void visit(ConstantTermExpression expr);
	
	void visit(CountExpression expr);
	
	void visit(DistinctExpression expr);
	
	void visit(ExistsExpression expr);
	
	void visit(FilterShapeExpression expr);
	
	void visit(FocusNodeExpression expr);
	
	void visit(FunctionExpression expr);

	void visit(GroupConcatExpression expr);
	
	void visit(IfExpression expr);
	
	void visit(IntersectionExpression expr);
	
	void visit(LimitExpression expr);
	
	void visit(MaxExpression expr);
	
	void visit(MinExpression expr);
	
	void visit(MinusExpression expr);
	
	void visit(OffsetExpression expr);
	
	void visit(OrderByExpression expr);
	
	void visit(PathExpression expr);
	
	void visit(SelectExpression expr);
	
	void visit(SumExpression expr);
	
	void visit(UnionExpression expr);

	void visitOther(NodeExpression expr);
}
