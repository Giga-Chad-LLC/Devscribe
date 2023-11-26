package components.parser.nodes.operators

import components.parser.nodes.AstNode
import components.parser.visitors.Visitor

class MultiplyNode(val lhs: AstNode, val rhs: AstNode) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}