package components.parser.nodes.operators

import components.parser.nodes.AstNode
import components.parser.visitors.Visitor


enum class UnaryOperatorType {
    NOT,
}


class UnaryOperatorNode(val expression: AstNode, val op: UnaryOperatorType) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}