package components.parser.nodes.literals

import components.parser.nodes.AstNode
import components.parser.visitors.Visitor

class BooleanTrueNode : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}