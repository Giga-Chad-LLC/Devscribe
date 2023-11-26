package components.parser.nodes.constructs

import components.parser.nodes.AstNode
import components.parser.visitors.Visitor

class ElseNode(val body: List<AstNode>) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}