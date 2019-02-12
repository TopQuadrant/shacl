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

public class NodeExpressionVisitorBase implements NodeExpressionVisitor {

	@Override
	public void visit(AskExpression expr) {
	}

	@Override
	public void visit(GroupConcatExpression expr) {
	}

	@Override
	public void visit(ConstantTermExpression expr) {
	}

	@Override
	public void visit(CountExpression expr) {
	}

	@Override
	public void visit(DistinctExpression expr) {
	}

	@Override
	public void visit(ExistsExpression expr) {
	}

	@Override
	public void visit(FilterShapeExpression expr) {
	}

	@Override
	public void visit(FocusNodeExpression expr) {
	}

	@Override
	public void visit(FunctionExpression expr) {
	}

	@Override
	public void visit(IfExpression expr) {
	}

	@Override
	public void visit(IntersectionExpression expr) {
	}

	@Override
	public void visit(LimitExpression expr) {
	}

	@Override
	public void visit(MaxExpression expr) {
	}

	@Override
	public void visit(MinExpression expr) {
	}

	@Override
	public void visit(MinusExpression expr) {
	}

	@Override
	public void visit(OffsetExpression expr) {
	}

	@Override
	public void visit(OrderByExpression expr) {
	}

	@Override
	public void visit(PathExpression expr) {
	}

	@Override
	public void visit(SelectExpression expr) {
	}

	@Override
	public void visit(SumExpression expr) {
	}

	@Override
	public void visit(UnionExpression expr) {
	}
	
	@Override
	public void visitOther(NodeExpression expr) {
	}
}
