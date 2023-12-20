package components.parser.nodes.constructs

import components.parser.nodes.AstNode
import components.parser.nodes.StatementNode
import components.parser.visitors.Visitor

class ScopeNode(val body: List<StatementNode>) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

}