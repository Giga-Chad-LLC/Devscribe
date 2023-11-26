package components.parser.nodes.constructs

import components.parser.nodes.AstNode
import components.parser.visitors.Visitor

class WhileLoopNode(
    val condition: AstNode,
    val body: AstNode,
) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}