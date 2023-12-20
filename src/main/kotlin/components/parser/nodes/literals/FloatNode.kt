package components.parser.nodes.literals

import components.parser.nodes.AstNode
import components.parser.visitors.Visitor

class FloatNode(val literal: String) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}