package components.parser.nodes

import components.parser.visitors.Visitor

class IdentifierNode(val identifier: String) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}