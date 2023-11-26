package components.parser.nodes

import components.parser.visitors.Visitor


interface AstNode {
    fun accept(visitor: Visitor)
}