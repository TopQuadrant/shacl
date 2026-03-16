package org.topbraid.shacl.expr;

import org.topbraid.shacl.expr.lib.*;

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
