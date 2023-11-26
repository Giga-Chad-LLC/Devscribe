package components.parser

import components.lexer.Token
import components.parser.nodes.AstNode

interface Parser {
    fun parse(tokens: List<Token>): AstNode
}