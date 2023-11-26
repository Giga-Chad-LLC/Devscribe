package components.parser.nodes.constructs

import components.parser.nodes.AstNode
import components.parser.visitors.Visitor

class ForLoopNode(
    val enteringStatement: AstNode,
    val condition: AstNode,
    val postIterationExpression: AstNode,
    val body: AstNode,
    ) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}