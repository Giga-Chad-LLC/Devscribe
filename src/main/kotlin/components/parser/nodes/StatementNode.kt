package components.parser.nodes

import components.parser.visitors.Visitor

class StatementNode(val statement: AstNode) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}