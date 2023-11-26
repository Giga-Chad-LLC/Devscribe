package components.parser.nodes

import components.parser.visitors.Visitor

class ProgramNode(val statements: List<StatementNode>) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}