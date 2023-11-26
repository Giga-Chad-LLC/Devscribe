package components.parser.nodes.constructs

import components.parser.nodes.AstNode
import components.parser.visitors.Visitor

class FunctionNode(
    val identifier: String,
    val arguments: List<AstNode>,
    // TODO: make it a single AstNode since we have ScopeNode?
    val body: List<AstNode>
    ) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}