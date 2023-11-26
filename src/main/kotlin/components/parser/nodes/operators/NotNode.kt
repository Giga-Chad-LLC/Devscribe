package components.parser.nodes.operators

import components.parser.nodes.AstNode
import components.parser.visitors.Visitor

class NotNode(val expression: AstNode) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}