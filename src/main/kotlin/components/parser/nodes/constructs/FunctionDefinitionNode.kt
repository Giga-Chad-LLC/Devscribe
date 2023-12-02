package components.parser.nodes.constructs

import components.parser.nodes.AstNode
import components.parser.nodes.IdentifierNode
import components.parser.visitors.Visitor

class FunctionDefinitionNode(
    val identifier: IdentifierNode,
    val arguments: List<AstNode>,
    val body: ScopeNode,
    ) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}