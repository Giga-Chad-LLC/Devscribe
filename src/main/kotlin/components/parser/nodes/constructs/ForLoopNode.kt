package components.parser.nodes.constructs

import components.parser.nodes.AstNode
import components.parser.nodes.StatementNode
import components.parser.visitors.Visitor

class ForLoopNode(
    val enteringStatement: StatementNode,
    val condition: AstNode, // TODO: Create ExpressionNode
    val postIterationExpression: AstNode, // TODO: Create ExpressionNode
    val body: ScopeNode,
    ) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}