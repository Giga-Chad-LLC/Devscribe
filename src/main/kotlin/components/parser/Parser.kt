package components.parser

import components.lexer.Token
import components.parser.nodes.AstNode

// TODO: parse() should return an array of encountered errors while parsing
interface Parser {
    fun parse(tokens: List<Token>): AstNode
}