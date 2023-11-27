package components.parser.nodes.constructs

import components.parser.nodes.AstNode
import components.parser.nodes.IdentifierNode
import components.parser.visitors.Visitor


/**
 * Represents definition (i.e. declaration and assignment at the same time) of a variable, e.g.:
 * 1. `var my_variable = 120;`
 * 2. `var str = "my str";`
 */
class DefinitionNode(val identifier: IdentifierNode, val expression: AstNode) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}