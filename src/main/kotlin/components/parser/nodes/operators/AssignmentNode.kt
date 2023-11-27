package components.parser.nodes.operators

import components.parser.nodes.AstNode
import components.parser.visitors.Visitor


/**
 * Represents assignment of a value to a variable, e.g.:
 * 1. `var my_variable; my_variable = 10;`
 * 2. `var my_variable = 10; my_variable = 12;`
 */
class AssignmentNode(val identifier: String, val expression: AstNode) : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}