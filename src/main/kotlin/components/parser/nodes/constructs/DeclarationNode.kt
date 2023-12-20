package components.parser.nodes.constructs

import components.parser.nodes.AstNode
import components.parser.nodes.IdentifierNode
import components.parser.visitors.Visitor


/**
 * Represents declaration of a variable, e.g.:
 * 1. `var my_variable;`
 * 2. `var str;`
 */
class DeclarationNode(val identifier: IdentifierNode) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}