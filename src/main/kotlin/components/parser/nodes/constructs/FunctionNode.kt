package components.parser.nodes.constructs

import components.parser.nodes.AstNode
import components.parser.visitors.Visitor

class FunctionNode(
    val identifier: String,
    val arguments: List<AstNode>, // TODO: those all are strings; change to List<String>?
    val body: AstNode
    ) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}