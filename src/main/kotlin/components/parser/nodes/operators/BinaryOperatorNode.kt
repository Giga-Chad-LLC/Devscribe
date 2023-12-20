package components.parser.nodes.operators

import components.parser.nodes.AstNode
import components.parser.visitors.Visitor

enum class BinaryOperatorType {
    PLUS,
    MINUS,
    DIVIDE,
    MULTIPLY,
    MODULO,
    LESS,
    GREATER,
    EQUALS,
    AND,
    OR,
}

class BinaryOperatorNode(val lhs: AstNode, val rhs: AstNode, val op: BinaryOperatorType) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}