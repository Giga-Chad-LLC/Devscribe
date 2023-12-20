package components.parser.nodes.auxiliary

import components.parser.nodes.AstNode
import components.parser.visitors.Visitor

class NoOperationNode : AstNode {
    override fun accept(visitor: Visitor) {
        /* No operation */
    }
}