package components.parser.nodes.auxiliary

import components.parser.nodes.AstNode
import components.parser.visitors.Visitor

class EndNode : AstNode {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}