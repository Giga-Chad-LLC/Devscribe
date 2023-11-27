package components.parser.nodes.constructs

import components.parser.nodes.AstNode
import components.parser.visitors.Visitor

class FunctionCallNode(val identifier: String, val arguments: List<AstNode>) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}